package sideproject.petmeeting.follow.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sideproject.petmeeting.common.Response;
import sideproject.petmeeting.common.ResponseResource;
import sideproject.petmeeting.common.StatusEnum;
import sideproject.petmeeting.common.exception.BusinessException;
import sideproject.petmeeting.follow.domain.Follow;
import sideproject.petmeeting.follow.domain.dto.FollowRequestDto;
import sideproject.petmeeting.follow.domain.dto.FollowingMemberListDto;
import sideproject.petmeeting.follow.service.FollowService;
import sideproject.petmeeting.member.domain.Member;
import sideproject.petmeeting.member.repository.MemberRepository;
import sideproject.petmeeting.security.TokenProvider;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.springframework.hateoas.MediaTypes.HAL_JSON_VALUE;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static sideproject.petmeeting.common.exception.ErrorCode.INVALID_TOKEN;
import static sideproject.petmeeting.common.exception.ErrorCode.NEED_LOGIN;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/follow", produces = HAL_JSON_VALUE)
public class FollowController {

    // == Dependency Injection ==//
    private final TokenProvider tokenProvider;
    private final FollowService followService;
    private final MemberRepository memberRepository;

    @PostMapping
    public ResponseEntity follow(@RequestBody @Valid FollowRequestDto followRequestDto,
                                 HttpServletRequest httpServletRequest) {
        Response response = new Response();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));

        Member member = checkAuthentication(httpServletRequest);
        Follow follow = followService.follow(followRequestDto, member);
        ResponseResource responseResource = new ResponseResource("정상적으로 팔로우가 완료되었습니다.");
        responseResource.add(linkTo(FollowController.class).withSelfRel());
        responseResource.add(linkTo(FollowController.class).slash(follow.getId()).withRel("팔로우 취소"));
        responseResource.add(linkTo(FollowController.class).slash("follow-member").withRel("get Following List"));
        responseResource.add(linkTo(FollowController.class).slash("followed-member").withRel("get Follower List"));

        response.setStatus(StatusEnum.CREATED);
        response.setMessage("정상적으로 팔로우가 완료되었습니다.");
        response.setData(responseResource);
        return new ResponseEntity<>(response, headers, HttpStatus.CREATED);
    }

    @DeleteMapping("/{followId}")
    public ResponseEntity unfollow(@PathVariable Long followId,
                                    HttpServletRequest httpServletRequest) {
        Response response = new Response();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));

        Member member = checkAuthentication(httpServletRequest);
        followService.unfollow(followId);

        ResponseResource responseResource = new ResponseResource("팔로우가 정상적으로 취소되었습니다.");
        responseResource.add(linkTo(FollowController.class).slash(followId).withSelfRel());
        responseResource.add(linkTo(FollowController.class).withRel("팔로우"));
        responseResource.add(linkTo(FollowController.class).slash("follow-member").withRel("get Following List"));
        responseResource.add(linkTo(FollowController.class).slash("followed-member").withRel("get Follower List"));
        response.setStatus(StatusEnum.OK);
        response.setMessage("팔로우가 정상적으로 취소되었습니다.");
        response.setData(responseResource);
        return new ResponseEntity<>(response, headers, HttpStatus.OK);
    }

    @GetMapping(value = "/follow-member")
    public ResponseEntity getFollowingMember(HttpServletRequest httpServletRequest) {

        Response response = new Response();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));

        Member member = checkAuthentication(httpServletRequest);
        List<FollowingMemberListDto> followingMemberList = followService.getFollowingMemberList(member);

        ResponseResource responseResource = new ResponseResource(followingMemberList);
        responseResource.add(linkTo(FollowController.class).slash("follow-member").withSelfRel());
        responseResource.add(linkTo(FollowController.class).slash("followed-member").withRel("get Follower List"));
        responseResource.add(linkTo(FollowController.class).withRel("follow"));
        responseResource.add(linkTo(FollowController.class).slash("unfollowId").withRel("Unfollow"));
        response.setStatus(StatusEnum.OK);
        response.setMessage("팔로잉 멤버 조회 완료");
        response.setData(responseResource);
        return new ResponseEntity<>(response, headers, HttpStatus.OK);
    }


    @GetMapping(value = "/follower")
    public ResponseEntity getFollowerMember(HttpServletRequest httpServletRequest) {
        Response response = new Response();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));

        Member member = checkAuthentication(httpServletRequest);
        List<FollowingMemberListDto> followerList = followService.getFollowerList(member);

        ResponseResource responseResource = new ResponseResource(followerList);
        responseResource.add(linkTo(FollowController.class).slash("followed-member").withSelfRel());
        responseResource.add(linkTo(FollowController.class).slash("follow-member").withRel("get Following List"));
        responseResource.add(linkTo(FollowController.class).withRel("follow"));
        responseResource.add(linkTo(FollowController.class).slash("unfollowId").withRel("Unfollow"));
        response.setStatus(StatusEnum.OK);
        response.setMessage("팔로워 조회 완료");
        response.setData(responseResource);
        return new ResponseEntity<>(response, headers, HttpStatus.OK);
    }


    private Member checkAuthentication(HttpServletRequest httpServletRequest) {
        if (httpServletRequest.getHeader("Authorization") == null || httpServletRequest.getHeader("Authorization").isEmpty()) {
            throw new BusinessException("로그인이 필요합니다.", NEED_LOGIN);
        }
        String accessToken = httpServletRequest.getHeader("Authorization").substring(7);
        tokenProvider.validateToken(accessToken);
        Member member = memberRepository.findByEmail(tokenProvider.getUserEmailByToken(accessToken)).orElse(null);
        if (member == null) {
            throw new BusinessException("올바르지 않은 토큰입니다.", INVALID_TOKEN);
        }
        return member;
    }
}
