package com.ssafy.keepham.domain.boxcontrol.producer;

import com.ssafy.keepham.domain.chat.entity.Message;
import com.ssafy.keepham.domain.chat.entity.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.PropertySource;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Component
@PropertySource("classpath:kafka.properties")
@Slf4j
public class BoxControlProducer {

    private final KafkaTemplate<String, Message> kafkaTemplate;
    private final MessageRepository messageRepository;

    @Value("${kafka.box-open.topic}")
    private String boxOpenTopic;

    @Value("${kafka.box-keypad.topic}")
    private String boxKeypadTopic;

    public void sendOpenMessageToBox(@Payload Message message){
        message.setTimestamp(LocalDateTime.now());
        log.info("open 메세지 : {}", message);
        messageRepository.save(message);
        kafkaTemplate.send(boxOpenTopic, message);
    }

    public void sendKeyPadPasswordMessageToBox(@Payload Message message){
        message.setTimestamp(LocalDateTime.now());
        log.info("keypad 메세지 : {}", message);
        messageRepository.save(message);
        kafkaTemplate.send(boxKeypadTopic, message);
    }
}
