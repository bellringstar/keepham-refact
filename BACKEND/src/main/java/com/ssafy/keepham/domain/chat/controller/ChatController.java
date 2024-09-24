package com.ssafy.keepham.domain.chat.controller;

import com.ssafy.keepham.common.api.Api;
import com.ssafy.keepham.domain.boxcontrol.producer.BoxControlProducer;
import com.ssafy.keepham.domain.chat.entity.Message;
import com.ssafy.keepham.domain.chat.entity.enums.Type;
import com.ssafy.keepham.domain.chat.dto.MessageResponse;
import com.ssafy.keepham.domain.chat.service.MessageService;
import com.ssafy.keepham.domain.chatroom.entity.enums.RoomUserStatus;
import com.ssafy.keepham.domain.chatroom.service.ChatRoomManager;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/api")
@RequiredArgsConstructor
public class ChatController {

    private final MessageService messageService;
    private final ChatRoomManager chatRoomManager;
    private final BoxControlProducer boxControlProducer;


    @Operation(summary = "roomId로 해당 채팅방의 채팅내역 조회")
    @GetMapping(value = "/chat-rooms/{roomId}/messages", produces = "application/json")
    public Api<List<MessageResponse>> getChatRoomMessages(@PathVariable Long roomId) {
        return Api.OK(messageService.findMessageLog(roomId));
    }

    @MessageMapping("/sendMessage/{roomId}")
    public void sendMessageToRoom(@Payload Message message, @DestinationVariable Long roomId){
        log.info("message : {}", message);
        switch (message.getType()) {
            case OPEN -> {
                log.info("OPEN 메세지");
                boxControlProducer.sendOpenMessageToBox(message);
                break;
            }
            case PASSWORD -> {
                log.info("KeyPad 암호");
                boxControlProducer.sendKeyPadPasswordMessageToBox(message);
                break;
            }
            default -> {
                log.info("{} 메세지", message.getType());
                chatRoomManager.sendMessageToRoom(message);
                break;
            }
        }
    }


    @MessageMapping("/joinUser/{roomId}")
    @SendTo("/subscribe/message/{roomId}")
    public Message joinUser(@Payload Message message, @DestinationVariable Long roomId) {
        log.info("joinUser/{roomId}로 발송된 메세지 : {}", message);
        var users = chatRoomManager.getAllUser(roomId);
        if (message.getType() == Type.ENTER) {
            log.info("User '{}' joined chat room {}", message.getAuthor(), roomId);
        } else if (message.getType() == Type.EXIT) {
            log.info("User '{}' left chat room {}", message.getAuthor(), roomId);
            chatRoomManager.userLeft(roomId, message.getAuthor(), RoomUserStatus.EXIT);
        }
        message.setAuthor("공지 : ");
        message.setUsers(users);
        log.info("message : {}", message);
        return message;
    }
}
