package sideproject.petmeeting.member.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberUpdateRequest {
    private String nickname;
    private String password;
    private String email;
    private String image;
}
