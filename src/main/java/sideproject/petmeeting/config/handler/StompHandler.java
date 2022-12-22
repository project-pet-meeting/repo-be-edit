package sideproject.petmeeting.config.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;
import sideproject.petmeeting.security.TokenProvider;

@RequiredArgsConstructor
@Component
public class StompHandler implements ChannelInterceptor {
    private TokenProvider tokenProvider;

    // websocket 을 통해 들어온 요청이 처리 되기 전에 실행 됨

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        if (StompCommand.CONNECT == accessor.getCommand()) {
            tokenProvider.validateToken(accessor.getFirstNativeHeader("Authorization").substring(7));
        }
        return message;
    }
}
