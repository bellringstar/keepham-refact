package com.ssafy.keepham.domain.user.dto.user.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {
    @Schema(description = "회원 비밀번호")
    String password;
    @Schema(description = "회원 새비밀번호")
    String newPassword;
    @Schema(description = "회원 이메일")
    String email;
    @Schema(description = "회원 전화번호")
    String tel;

}
