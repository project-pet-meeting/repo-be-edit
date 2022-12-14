package sideproject.petmeeting.member.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberDetailRequestDto {
    @NotEmpty
    private String nickname;
    @NotEmpty
    private String location;

}
