package sideproject.petmeeting.follow.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sideproject.petmeeting.common.exception.BusinessException;
import sideproject.petmeeting.follow.domain.Follow;
import sideproject.petmeeting.follow.domain.dto.FollowRequestDto;
import sideproject.petmeeting.follow.domain.dto.FollowingMemberListDto;
import sideproject.petmeeting.follow.repository.FollowRepository;
import sideproject.petmeeting.member.domain.Member;
import sideproject.petmeeting.member.repository.MemberRepository;

import java.util.ArrayList;
import java.util.List;

import static sideproject.petmeeting.common.exception.ErrorCode.FOLLOW_NOT_EXIST;
import static sideproject.petmeeting.common.exception.ErrorCode.MEMBER_NOT_EXIST;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FollowService {

    // == Dependency Injection ==//
    private final MemberRepository memberRepository;
    private final FollowRepository followRepository;

    @Transactional
    public Follow follow(FollowRequestDto followRequestDto, Member member) {
        Member followMember = memberRepository.findById(followRequestDto.getFollowMemberId()).orElseThrow(
                () -> new BusinessException("회원이 존재하지 않습니다.", MEMBER_NOT_EXIST)
        );

        Follow follow = Follow.builder()
                .following(followMember)
                .follower(member)
                .build();
        return followRepository.save(follow);
    }

    @Transactional
    public void unfollow(Long followId) {
        Follow follow = followRepository.findById(followId).orElseThrow(
                () -> new BusinessException("해당 팔로우가 존재하지 않습니다.", FOLLOW_NOT_EXIST)
        );
        followRepository.delete(follow);
    }

    public List<FollowingMemberListDto> getFollowingMemberList(Member member) {
        List<Follow> followingMemberList = followRepository.findAllByFollower(member);
        List<FollowingMemberListDto> memberList = new ArrayList<>();
        for (Follow follow : followingMemberList) {
            memberList.add(FollowingMemberListDto.builder()
                    .memberId(follow.getFollowing().getId())
                    .memberNickname(follow.getFollowing().getNickname())
                    .memberImage(follow.getFollowing().getImage())
                    .build());
        }
        return memberList;
    }

    public List<FollowingMemberListDto> getFollowerList(Member member) {
        List<Follow> followerList = followRepository.findAllByFollowing(member);
        List<FollowingMemberListDto> memberList = new ArrayList<>();
        for (Follow follow : followerList) {
            memberList.add(FollowingMemberListDto.builder()
                    .memberId(follow.getFollowing().getId())
                    .memberNickname(follow.getFollowing().getNickname())
                    .memberImage(follow.getFollowing().getImage())
                    .build());
        }
        return memberList;
    }
}
