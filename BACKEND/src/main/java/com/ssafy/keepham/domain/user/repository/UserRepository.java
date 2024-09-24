package com.ssafy.keepham.domain.user.repository;

import com.ssafy.keepham.domain.user.common.AccountStatus;
import com.ssafy.keepham.domain.user.common.UserRole;
import com.ssafy.keepham.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUserIdAndAccountStatus(String userId, AccountStatus accountStatus);
    List<User> findAllByUserRole(UserRole role);
    Optional<User> findFirstByNickName(String nickname);
    Optional<User> findByNickName(String nickName);
}
