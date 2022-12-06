package sideproject.petmeeting.member.dto.response;

import lombok.Getter;

@Getter
public class NicknameResponseDto {
    private String nickname;

    public NicknameResponseDto(String nickname) {
        this.nickname = nickname;
    }
}
