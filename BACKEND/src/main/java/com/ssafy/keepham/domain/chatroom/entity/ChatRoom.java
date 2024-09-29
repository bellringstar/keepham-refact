package com.ssafy.keepham.domain.chatroom.entity;

import com.ssafy.keepham.common.error.ChatRoomError;
import com.ssafy.keepham.common.exception.ApiException;
import com.ssafy.keepham.domain.box.entity.Box;
import com.ssafy.keepham.domain.chatroom.entity.enums.ChatRoomStatus;
import com.ssafy.keepham.domain.chatroom.entity.enums.RoomPhase;
import com.ssafy.keepham.domain.common.BaseEntity;
import com.ssafy.keepham.domain.store.entity.Store;
import com.ssafy.keepham.domain.user.entity.User;
import jakarta.persistence.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "chatroom")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = {"store", "box"})
public class ChatRoom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(length = 255, nullable = false, unique = true)
    private String title;

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private ChatRoomStatus status;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "box_id", nullable = false)
    private Box box;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_user_id", nullable = false)
    private User superUser;

    private int extensionCount;

    @Column(nullable = false)
    private int maxPeopleNumber;

    @Column(nullable = false)
    private boolean locked;

    @Column(length = 64, nullable = false)
    private String password;

    @Column(length = 16, nullable = false)
    private String salt;

    @Column(nullable = true)
    private LocalDateTime closedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomPhase phase;

    private boolean isDeleted = false;

    @Builder
    private ChatRoom(String title, Store store, Box box, int maxPeopleNumber, User superUser, boolean locked, String password) {
        this.title = Objects.requireNonNull(title, "제목은 null일 수 없습니다.");
        this.store = Objects.requireNonNull(store, "store는 null일 수 없습니다.");
        this.box = Objects.requireNonNull(box, "Box는 null일 수 없습니다.");
        this.maxPeopleNumber = validateMaxPeopleNumber(maxPeopleNumber);
        this.superUser = Objects.requireNonNull(superUser, "Super user는 null일 수 없습니다.");
        this.locked = locked;
        this.password = locked ? Objects.requireNonNull(password, "비밀방에 password는 필수입니다.") : "";
        setPassword(password);
        this.status = ChatRoomStatus.OPEN;
        this.extensionCount = 0;
        this.phase = RoomPhase.CREATED;
    }

    public void setPassword(String password) {
        this.salt = generateSalt();
        this.password = hashPassword(password, this.salt);
    }

    public boolean checkPassword(String inputPassword) {
        String hashedInput = hashPassword(inputPassword, this.salt);
        return this.password.equals(hashedInput);
    }

    public void close() {
        if (this.status != ChatRoomStatus.OPEN) {
            throw new ApiException(ChatRoomError.BAD_REQUEST, "열려있는 채팅방만 닫을 수 있습니다.");
        }
        this.status = ChatRoomStatus.CLOSE;
        this.closedAt = LocalDateTime.now();
    }

    public void extendTime(long hours) {
        if (this.extensionCount >= 3) {
            throw new ApiException(ChatRoomError.BAD_REQUEST, "3회 이상 연장할 수 없습니다.");
        }
        this.closedAt = this.closedAt.plusHours(hours);
        this.extensionCount++;
    }

    public void changeSuperUser(User newSuperUser) {
        this.superUser = Objects.requireNonNull(newSuperUser, "Super user ID는 null일 수 없습니다.");
    }

    public void updatePhase(RoomPhase newPhase) {
        validatePhaseTransition(this.phase, newPhase);
        this.phase = newPhase;
    }

    private void validatePhaseTransition(RoomPhase currentPhase, RoomPhase newPhase) {
        if (newPhase.getStep() <= currentPhase.getStep()) {
            throw new ApiException(ChatRoomError.BAD_REQUEST, currentPhase + "에서 " + newPhase + "는 변경할 수 없습니다.");
        }
    }

    private int validateMaxPeopleNumber(int maxPeopleNumber) {
        if (maxPeopleNumber < 1 || maxPeopleNumber > 12) {
            throw new ApiException(ChatRoomError.BAD_REQUEST, "최대 인원은 1 ~ 12 입니다");
        }
        return maxPeopleNumber;
    }

    private String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    private String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes());
            byte[] hashedPassword = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashedPassword);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to hash password", e);
        }
    }
}
