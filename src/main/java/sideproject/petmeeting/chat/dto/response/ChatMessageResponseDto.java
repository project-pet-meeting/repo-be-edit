package sideproject.petmeeting.chat.dto.response;

import lombok.Builder;
import lombok.Getter;
import sideproject.petmeeting.chat.domain.MessageType;

@Builder
@Getter
public class ChatMessageResponseDto {
    private Long id;
    private MessageType type; // 메시지 타입
    private String roomId; // 방번호
    private String sender; // 메시지 보낸사람
    private String senderImage; // 보낸 사람 이미지
    private String message; // 메시지
}
