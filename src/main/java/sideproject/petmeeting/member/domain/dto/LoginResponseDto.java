package sideproject.petmeeting.member.domain.dto;

import lombok.Getter;

@Getter
public class LoginResponseDto {
    String nickname;
    public LoginResponseDto(String nickname) {
        this.nickname = nickname;
    }
}
