package sideproject.petmeeting.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberDto {
    @NotEmpty
    private String nickname;
    @NotEmpty
    private String password;
    @NotEmpty
    private String email;
    @NotEmpty
    private String image;
}
