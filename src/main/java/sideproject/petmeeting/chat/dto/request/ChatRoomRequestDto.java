package sideproject.petmeeting.chat.dto.request;

import lombok.Getter;

@Getter
public class ChatRoomRequestDto {
    String name;

    public ChatRoomRequestDto() {
    }

    public ChatRoomRequestDto(String name) {
        this.name = name;
    }
}
