package sideproject.petmeeting.chat.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import sideproject.petmeeting.chat.domain.ChatMessage;
import sideproject.petmeeting.chat.domain.MessageType;
import sideproject.petmeeting.chat.repository.ChatMessageRepository;
import sideproject.petmeeting.chat.repository.ChatRoomRepository;
import sideproject.petmeeting.security.TokenProvider;

@RequiredArgsConstructor
@Controller
@Slf4j
public class MessageController {
    private final ChatMessageRepository chatMessageRepository;

    private final ChatRoomRepository chatRoomRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final TokenProvider jwtTokenProvider;
    private final ChannelTopic channelTopic;

    /**
     * websocket "/pub/chat/message"로 들어오는 메시징을 처리한다.
     */
    @MessageMapping("/chat/message")
    @Transactional
    public void message(ChatMessage message) {
        log.info(message.getMessage());
        // 채팅방 입장시에는 대화명과 메시지를 자동으로 세팅한다.
        if (MessageType.ENTER.equals(message.getType())) {
            message.setSender("[알림]");
            message.setMessage(message.getSender() + "님이 입장하셨습니다.");
        }
        // Websocket에 발행된 메시지를 redis로 발행(publish)
        log.info("Message controller topic : {}", channelTopic.getTopic());
        redisTemplate.convertAndSend(channelTopic.getTopic(), message);
        ChatMessage MySQLMessage = ChatMessage.builder()
                .type(message.getType())
                .roomId(message.getRoomId())
                .sender(message.getSender())
                .message(message.getMessage())
                .build();
        chatMessageRepository.save(MySQLMessage);
    }
}
