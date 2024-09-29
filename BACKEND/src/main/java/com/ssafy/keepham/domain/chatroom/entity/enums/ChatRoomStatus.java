package com.ssafy.keepham.domain.chatroom.entity.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ChatRoomStatus {

    OPEN("진행중"),
    CLOSE("종료");


    private final String description;
}
