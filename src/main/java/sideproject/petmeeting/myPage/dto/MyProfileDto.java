package sideproject.petmeeting.myPage.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MyProfileDto {
    private Long id;
    private String nickname;
    private String email;
    private String location;
    private String image;
}
