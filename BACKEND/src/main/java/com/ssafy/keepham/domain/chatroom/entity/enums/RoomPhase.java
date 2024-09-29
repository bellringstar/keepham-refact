package com.ssafy.keepham.domain.chatroom.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum RoomPhase {
    CREATED(0),
    ORDER_STARTED(1),
    ORDER_COMPLETED(2),
    DELIVERY_IN_PROGRESS(3),
    DELIVERED(4);

    private final int step;
}
