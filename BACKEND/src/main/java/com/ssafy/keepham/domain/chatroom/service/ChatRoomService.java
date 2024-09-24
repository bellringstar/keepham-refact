package com.ssafy.keepham.domain.chatroom.service;

import com.ssafy.keepham.common.error.ErrorCode;
import com.ssafy.keepham.common.exception.ApiException;
import com.ssafy.keepham.domain.box.entity.Box;
import com.ssafy.keepham.domain.box.repository.BoxRepository;
import com.ssafy.keepham.domain.chatroom.converter.ChatRoomConverter;
import com.ssafy.keepham.domain.chatroom.dto.ChatRoomRequest;
import com.ssafy.keepham.domain.chatroom.dto.ChatRoomResponse;
import com.ssafy.keepham.domain.chatroom.entity.ChatRoomEntity;
import com.ssafy.keepham.domain.chatroom.entity.enums.ChatRoomStatus;
import com.ssafy.keepham.domain.chatroom.repository.ChatRoomRepository;
import com.ssafy.keepham.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomConverter chatRoomConverter;
    private final ChatRoomManager chatRoomManager;
    private final BoxRepository boxRepository;
    private final UserService userService;


    @Transactional
    public ChatRoomResponse createRoom(ChatRoomRequest chatRoomRequest){
        var userInfo = userService.getLoginUserInfo();
        var userNickName = userInfo.getNickName();
        var entity = chatRoomConverter.toEntity(chatRoomRequest);
        var box = boxRepository.findFirstById(chatRoomRequest.getBoxId());
        if (box.isUsed()) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "이미 사용중인 box입니다.");
        }
        box.setUsed(true);
        entity.setBox(box);
        return Optional.ofNullable(entity)
                .map(it -> {
                    it.setStatus(ChatRoomStatus.OPEN);
                    var newEntity = chatRoomRepository.save(it);
                    newEntity.setClosedAt(newEntity.getCreatedAt().plusHours(3));
                    newEntity.setSuperUserId(userNickName);
                    chatRoomManager.userJoin(newEntity.getId(), userService.getLoginUserInfo().getNickName());
                    return chatRoomConverter.toResponse(newEntity);
                })
                .orElseThrow(() -> new ApiException(ErrorCode.BAD_REQUEST));
    }

    public List<ChatRoomResponse> openedRoom(ChatRoomStatus status, int page, int pageSize){
        Pageable pageable = PageRequest.of(page-1, pageSize);
        Page<ChatRoomEntity> chatRoomEntityPage = chatRoomRepository.findAllByStatusOrderByCreatedAtDesc(status, pageable);

        List<ChatRoomEntity> chatRooms = chatRoomEntityPage.getContent();
        return chatRooms.stream().map(chatRoomConverter::toResponse)
                .map(it -> {
                    var currentNumber = chatRoomManager.getUserCountInChatRoom(it.getId());
                    it.setCurrentPeopleNumber(currentNumber);
                    return it;
                })
                .collect(Collectors.toList());
    }

    public ChatRoomResponse findRoomById(Long roomId, ChatRoomStatus status){
        ChatRoomEntity chatRoomEntity = Optional.ofNullable(chatRoomRepository.findFirstByIdAndStatus(roomId,status))
                .orElseThrow(()->new ApiException(ErrorCode.BAD_REQUEST, "존재하지 않는 방입니다."));
        var response = chatRoomConverter.toResponse(chatRoomEntity);
        response.setCurrentPeopleNumber(chatRoomManager.getUserCountInChatRoom(response.getId()));
        return response;

    }

    public List<ChatRoomResponse> findAllRoomByZipCode(ChatRoomStatus status, int page, int pageSize, String zipCode){
        Pageable pageable = PageRequest.of(page-1, pageSize);
        List<Box> boxes = boxRepository.getAllzipcode(zipCode);
        List<Long> box_ids = boxes.stream().map(Box::getId).toList();
        log.info("box_ids : {}",box_ids);
        Page<ChatRoomEntity> chatRoomEntityPage = chatRoomRepository.findAllByStatusAndBoxIdsOrderByCreatedAtDesc("OPEN", box_ids, pageable);
        System.out.println(chatRoomEntityPage.getContent());
        List<ChatRoomEntity> chatRooms = chatRoomEntityPage.getContent();
        return chatRooms.stream().map(chatRoomConverter::toCategoryResponse)
                .collect(Collectors.toList());
    }

    // 채팅방 상태 close로 변경
    @Transactional
    public void closeRoom(Long roomId){
        var user = userService.getLoginUserInfo();
        var room = chatRoomRepository.findFirstByIdAndStatus(roomId, ChatRoomStatus.OPEN);
        if (!user.getNickName().equals(room.getSuperUserId())){
            throw new ApiException(ErrorCode.BAD_REQUEST, "방 종료는 방장만 할 수 있습니다.");
        }
        chatRoomManager.allUserClear(roomId);
        room.getBox().setUsed(false);
        room.setStatus(ChatRoomStatus.CLOSE);
    }


    public ChatRoomResponse changeStep(Long roomId, int step) {
        var entity = chatRoomRepository.findFirstByIdAndStatus(roomId, ChatRoomStatus.OPEN);
        entity.setStep(step);
        var newEntity = chatRoomRepository.save(entity);
        return chatRoomConverter.toResponse(newEntity);
    }
}
