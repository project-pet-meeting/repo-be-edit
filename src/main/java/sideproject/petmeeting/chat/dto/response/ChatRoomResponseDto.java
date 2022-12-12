package sideproject.petmeeting.chat.dto.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ChatRoomResponseDto {
    private Long id;
    private Long postId;
    private String roomName;
}
