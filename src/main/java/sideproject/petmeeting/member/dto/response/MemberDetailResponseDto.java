package sideproject.petmeeting.member.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sideproject.petmeeting.member.dto.request.MemberDetailRequestDto;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberDetailResponseDto {

    private String nickname;

    private String location;

    public MemberDetailResponseDto(MemberDetailRequestDto memberDetailRequestDto) {
        this.nickname = memberDetailRequestDto.getNickname();
        this.location = memberDetailRequestDto.getLocation();
    }
}
