package sideproject.petmeeting.member.dto.request;

import lombok.*;

import javax.validation.constraints.NotNull;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDto {
    @NotNull
    private String email;
    @NotNull
    private String password;
}
