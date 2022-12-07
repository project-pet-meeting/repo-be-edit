package sideproject.petmeeting.myPage.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sideproject.petmeeting.common.exception.BusinessException;
import sideproject.petmeeting.common.exception.ErrorCode;
import sideproject.petmeeting.meeting.domain.Meeting;
import sideproject.petmeeting.meeting.dto.MeetingResponseDto;
import sideproject.petmeeting.meeting.repository.MeetingRepository;
import sideproject.petmeeting.member.domain.Member;
import sideproject.petmeeting.myPage.dto.MyHeartPostDto;
import sideproject.petmeeting.myPage.dto.MyMeetingDto;
import sideproject.petmeeting.myPage.dto.MyPostDto;
import sideproject.petmeeting.myPage.dto.MyProfileDto;
import sideproject.petmeeting.post.domain.HeartPost;
import sideproject.petmeeting.post.domain.Post;
import sideproject.petmeeting.post.dto.PostResponseDto;
import sideproject.petmeeting.post.repository.HeartPostRepository;
import sideproject.petmeeting.post.repository.PostRepository;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class MyPageService {
    private final PostRepository postRepository;
    private final MeetingRepository meetingRepository;
    private final HeartPostRepository heartPostRepository;

    /**
     * 마이페이지 - 내 정보 조회
     * @param member : 사용자
     * @return : 사용자 정보
     */
    public MyProfileDto getProfile(Member member) {

        return MyProfileDto.builder()
                .id(member.getId())
                .nickname(member.getNickname())
                .email(member.getEmail())
                .image(member.getImage())
                .build();
    }


    /**
     * 마이페이지 - 내가 작성한 게시글 조회
     * @param member : 사용자
     * @return : 사용자의 작성한 게시글 리스트
     */
    @Transactional
    public MyPostDto getMyPost(Member member) {
        List<Post> myPostList = postRepository.findAllByMemberId(member.getId());
        if (null == myPostList) {
            throw new BusinessException("내가 작성한 게시글이 없습니다.", ErrorCode.MY_POST_NOT_EXIST);
        }

        List<PostResponseDto> postResponseDtoList = new ArrayList<>();
        for (Post post : myPostList) {
            postResponseDtoList.add(
                    PostResponseDto.builder()
                            .id(post.getId())
                            .category(post.getCategory())
                            .title(post.getTitle())
                            .content(post.getContent())
                            .imageUrl(post.getImageUrl())
                            .numHeart(post.getNumHeart())
                            .authorId(post.getMember().getId())
                            .authorNickname(post.getMember().getNickname())
                            .authorImageUrl(post.getMember().getImage())
                            .createdAt(post.getCreatedAt())
                            .modifiedAt(post.getModifiedAt())
                            .build()
            );
        }


        return MyPostDto.builder()
                .myPostList(postResponseDtoList)
                .build();
    }


    /**
     * 마이페이지 - 내가 만든 모임 조회
     * @param member : 사용자
     * @return : 사용자가 만든 모임 리스트
     */
    @Transactional
    public MyMeetingDto getMyMeeting(Member member) {
        List<Meeting> myMeetingList = meetingRepository.findAllByMemberId(member.getId());
        if (null == myMeetingList) {
            throw new BusinessException("내가 만든 모임이 없습니다.", ErrorCode.MY_MEETING_NOT_EXIST);
        }


        List<MeetingResponseDto> meetingResponseDtoList = new ArrayList<>();
        for (Meeting meeting : myMeetingList) {
            meetingResponseDtoList.add(
                    MeetingResponseDto.builder()
                            .id(meeting.getId())
                            .title(meeting.getTitle())
                            .content(meeting.getContent())
                            .imageUrl(meeting.getImageUrl())
                            .address(meeting.getAddress())
                            .coordinateX(meeting.getCoordinateX())
                            .coordinateY(meeting.getCoordinateY())
                            .placeName(meeting.getPlaceName())
                            .time(meeting.getTime())
                            .recruitNum(meeting.getRecruitNum())
                            .species(meeting.getSpecies())
                            .authorId(meeting.getMember().getId())
                            .authorNickname(meeting.getMember().getNickname())
                            .authorImageUrl(meeting.getMember().getImage())
                            .createdAt(meeting.getCreatedAt())
                            .modifiedAt(meeting.getModifiedAt())
                            .build()
            );
        }


        return MyMeetingDto.builder()
                .myMeetingList(meetingResponseDtoList)
                .build();
    }


    /**
     * 마이페이지 - 내가 '좋아요'한 게시글 조회
     * @param member : 사용자
     * @return : 사용자가 '좋아요'한 게시글 리스트
     */
    @Transactional
    public MyHeartPostDto getMyHeartPost(Member member) {
        List<HeartPost> myHeartPostList = heartPostRepository.findAllByMemberId(member.getId());
        if (null == myHeartPostList) {
            throw new BusinessException("내가 '좋아요'한 게시글이 없습니다.", ErrorCode.MY_HEART_POST_NOT_EXIST);
        }

        List<PostResponseDto> postResponseDtoList = new ArrayList<>();
        for (HeartPost heartPost : myHeartPostList) {
            postResponseDtoList.add(
                    PostResponseDto.builder()
                            .id(heartPost.getId())
                            .category(heartPost.getPost().getCategory())
                            .title(heartPost.getPost().getTitle())
                            .content(heartPost.getPost().getContent())
                            .imageUrl(heartPost.getPost().getImageUrl())
                            .numHeart(heartPost.getPost().getNumHeart())
                            .authorId(heartPost.getMember().getId())
                            .authorNickname(heartPost.getMember().getNickname())
                            .authorImageUrl(heartPost.getMember().getImage())
                            .createdAt(heartPost.getPost().getCreatedAt())
                            .modifiedAt(heartPost.getPost().getModifiedAt())
                            .build()
            );
        }

        return MyHeartPostDto.builder()
                .myHeartPostList(postResponseDtoList)
                .build();
    }

}
