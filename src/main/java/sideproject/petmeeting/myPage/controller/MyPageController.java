package sideproject.petmeeting.myPage.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sideproject.petmeeting.common.Response;
import sideproject.petmeeting.common.ResponseResource;
import sideproject.petmeeting.common.StatusEnum;
import sideproject.petmeeting.member.controller.MemberController;
import sideproject.petmeeting.myPage.service.MyPageService;
import sideproject.petmeeting.myPage.dto.MyHeartPostDto;
import sideproject.petmeeting.myPage.dto.MyMeetingDto;
import sideproject.petmeeting.myPage.dto.MyPostDto;
import sideproject.petmeeting.myPage.dto.ProfileDto;
import sideproject.petmeeting.security.UserDetailsImpl;


import static org.springframework.hateoas.MediaTypes.HAL_JSON_VALUE;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/mypage", produces = HAL_JSON_VALUE)
public class MyPageController {

    private final MyPageService myPageService;

    /**
     * 마이페이지 - 내 정보 조회
     * @param userDetails : 사용자
     * @return : 내 정보 조회 성공 응답
     */
    @GetMapping
    public ResponseEntity<Object> getMyProfile(@AuthenticationPrincipal UserDetailsImpl userDetails){

        ProfileDto profileDto = myPageService.getProfile(userDetails.getMember());

        ResponseResource responseResource = new ResponseResource(profileDto);
        responseResource.add(linkTo(MyPageController.class).withSelfRel());
        responseResource.add(linkTo(MemberController.class).withRel("member-edit"));

        Response response = new Response(StatusEnum.OK, "내 프로필 조회 성공", responseResource);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 타유저 프로필 조회
     * @param memberId : 조회할 회원 Id
     * @return : 타회원 정보
     */
    @GetMapping("/{memberId}")
    public ResponseEntity<Object> getMemberProfile(@PathVariable Long memberId) {

        ProfileDto profileDto = myPageService.getMemberProfile(memberId);

        ResponseResource responseResource = new ResponseResource(profileDto);
        responseResource.add(linkTo(MyPageController.class).slash(memberId).withSelfRel());

        Response response = new Response(StatusEnum.OK, "회원 프로필 조회 성공", responseResource);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    /**
     * 마이페이지 - 내가 작성한 게시글 조회
     * @param userDetails : 사용자
     * @return : 내가 작성한 게시글 조회 성공 응답
     */
    @GetMapping("/post")
    public ResponseEntity<Object> getMyPost(@AuthenticationPrincipal UserDetailsImpl userDetails){

        MyPostDto myPostDto = myPageService.getMyPost(userDetails.getMember());

        ResponseResource responseResource = new ResponseResource(myPostDto);
        responseResource.add(linkTo(MyPageController.class).slash("post").withSelfRel());

        Response response = new Response(StatusEnum.OK, "내가 작성한 게시글 조회 성공", responseResource);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    /**
     * 마이페이지 - 내가 만든 모임 조회
     * @param userDetails : 사용자
     * @return : 내가 만든 모임 조회 성공 응답
     */
    @GetMapping("/meeting")
    public ResponseEntity<Object> getMyMeeting(@AuthenticationPrincipal UserDetailsImpl userDetails){

        MyMeetingDto myMeetingDto = myPageService.getMyMeeting(userDetails.getMember());

        ResponseResource responseResource = new ResponseResource(myMeetingDto);
        responseResource.add(linkTo(MyPageController.class).slash("meeting").withSelfRel());

        Response response = new Response(StatusEnum.OK, "내가 만든 모임 조회 성공", responseResource);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    /**
     * 마이페이지 - 내가 '좋아요'한 게시글 조회
     * @param userDetails : 사용자
     * @return : 내가 '좋아요'한 게시글 조회 성공 응답
     */
    @GetMapping("/heart")
    public ResponseEntity<Object> getMyHeartPost(@AuthenticationPrincipal UserDetailsImpl userDetails){

        MyHeartPostDto myHeartPostDto = myPageService.getMyHeartPost(userDetails.getMember());

        ResponseResource responseResource = new ResponseResource(myHeartPostDto);
        responseResource.add(linkTo(MyPageController.class).slash("heart").withSelfRel());

        Response response = new Response(StatusEnum.OK, "내가 '좋아요'한 게시글 조회 성공", responseResource);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


}
