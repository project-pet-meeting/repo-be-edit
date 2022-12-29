package sideproject.petmeeting.follow.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class FollowingMemberListDto {

    private Long memberId;
    private String memberNickname;
    private String memberImage;
}
