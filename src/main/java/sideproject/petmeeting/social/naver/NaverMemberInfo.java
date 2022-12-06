package sideproject.petmeeting.social.naver;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class NaverMemberInfo {
    public String email;
    public String imageUrl;
    public String nickname;
}
