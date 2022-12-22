package sideproject.petmeeting.chat.pubsub;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import sideproject.petmeeting.chat.domain.ChatMessage;

@Slf4j
@RequiredArgsConstructor
@Service
public class RedisSubscriber {

    private final ObjectMapper objectMapper;
    private final SimpMessageSendingOperations messagingTemplate;

    /**
     * Redis 에서 메세지가 발행되면 대기하고 있던 Redis Subscriber 가 해당 메세지를 받아 처리
     */
    public void sendMessage(String publishMessage) {
        log.info("Redis Subscriber 실행");
        try {
            ChatMessage chatMessage = objectMapper.readValue(publishMessage, ChatMessage.class);
            log.info(chatMessage.getRoomId());
            messagingTemplate.convertAndSend("/sub/chat/room/" + chatMessage.getRoomId(), chatMessage);
        } catch (JsonProcessingException e) {
            log.error("메세지 전송 오류 발생", e);
        }
    }
}
