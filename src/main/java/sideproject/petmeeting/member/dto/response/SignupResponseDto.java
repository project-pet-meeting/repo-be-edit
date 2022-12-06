package sideproject.petmeeting.member.dto.response;

import lombok.Getter;


@Getter
public class SignupResponseDto {
    private Long id;

    public SignupResponseDto(Long id) {
        this.id = id;
    }
}
