package sideproject.petmeeting.follow.domain.dto;

import lombok.Getter;

import javax.validation.constraints.NotNull;

@Getter
public class FollowRequestDto {
    @NotNull
    private Long followMemberId;

    public FollowRequestDto() {
    }
    public FollowRequestDto(Long followMemberId) {
        this.followMemberId = followMemberId;
    }
}
