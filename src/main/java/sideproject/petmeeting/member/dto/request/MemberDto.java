package sideproject.petmeeting.member.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberDto {
    @NotEmpty(message = "빈 값일 수 없습니다.")
    private String nickname;
    @NotEmpty
    private String password;
    @NotEmpty
    private String email;
}
