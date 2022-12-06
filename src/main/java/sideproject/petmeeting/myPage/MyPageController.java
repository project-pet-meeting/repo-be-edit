package sideproject.petmeeting.myPage;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sideproject.petmeeting.common.Response;
import sideproject.petmeeting.common.ResponseResource;
import sideproject.petmeeting.common.StatusEnum;
import sideproject.petmeeting.myPage.dto.MyHeartPostDto;
import sideproject.petmeeting.myPage.dto.MyMeetingDto;
import sideproject.petmeeting.myPage.dto.MyPostDto;
import sideproject.petmeeting.myPage.dto.MyProfileDto;
import sideproject.petmeeting.security.UserDetailsImpl;


import static org.springframework.hateoas.MediaTypes.HAL_JSON_VALUE;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api", produces = HAL_JSON_VALUE)
public class MyPageController {

    private final MyPageService myPageService;

    /**
     * 마이페이지 - 내 정보 조회
     * @param userDetails :
     * @return :
     */
    @GetMapping("/mypage")
    public ResponseEntity<Object> getMyProfile(@AuthenticationPrincipal UserDetailsImpl userDetails){

        MyProfileDto myProfileDto = myPageService.getProfile(userDetails.getMember());

        ResponseResource responseResource = new ResponseResource(myProfileDto);
        responseResource.add(linkTo(MyPageController.class).withSelfRel());
        responseResource.add(linkTo(MyPageController.class).slash("member").withRel("member-edit"));
        responseResource.add(linkTo(MyPageController.class).slash("member").slash("signout").withRel("member-delete"));

        Response response = new Response(StatusEnum.OK, "내 프로필 조회 성공", responseResource);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    /**
     * 마이페이지 - 내가 작성한 게시글 조회
     * @param userDetails
     * @return
     */
    @GetMapping("/mypage/post")
    public ResponseEntity<Object> getMyPost(@AuthenticationPrincipal UserDetailsImpl userDetails){

        MyPostDto myPostDto = myPageService.getMyPost(userDetails.getMember());

        ResponseResource responseResource = new ResponseResource(myPostDto);
        responseResource.add(linkTo(MyPageController.class).withSelfRel());

        Response response = new Response(StatusEnum.OK, "내가 작성한 게시글 조회 성공", responseResource);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    /**
     * 내가 만든 모임 조회
     * @param userDetails
     * @return
     */
    @GetMapping("/mypage/meeting")
    public ResponseEntity<Object> getMyMeeting(@AuthenticationPrincipal UserDetailsImpl userDetails){

        MyMeetingDto myMeetingDto = myPageService.getMyMeeting(userDetails.getMember());

        ResponseResource responseResource = new ResponseResource(myMeetingDto);
        responseResource.add(linkTo(MyPageController.class).withSelfRel());

        Response response = new Response(StatusEnum.OK, "내가 만든 모임 조회 성공", responseResource);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    /**
     * 마이페이지 - 내가 좋아요한 게시글 조회
     * @param userDetails
     * @return
     */
    @GetMapping("/mypage/heart")
    public ResponseEntity<Object> getMyHeartPost(@AuthenticationPrincipal UserDetailsImpl userDetails){

        MyHeartPostDto myHeartPostDto = myPageService.getMyHeartPost(userDetails.getMember());

        ResponseResource responseResource = new ResponseResource(myHeartPostDto);
        responseResource.add(linkTo(MyPageController.class).withSelfRel());

        Response response = new Response(StatusEnum.OK, "내가 '좋아요'한 게시글 조회 성공", responseResource);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


}
