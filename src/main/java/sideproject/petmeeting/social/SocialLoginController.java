package sideproject.petmeeting.social;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sideproject.petmeeting.common.Response;
import sideproject.petmeeting.common.ResponseResource;
import sideproject.petmeeting.common.StatusEnum;
import sideproject.petmeeting.member.domain.Member;
import sideproject.petmeeting.social.kakao.KakaoLoginService;
import sideproject.petmeeting.social.kakao.SocialLoginResponse;
import sideproject.petmeeting.social.naver.NaverLoginService;

import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Slf4j
@RestController
@RequiredArgsConstructor
public class SocialLoginController {
    private final KakaoLoginService kakaoLoginService;
    private final NaverLoginService naverLoginService;

    @GetMapping(path = "/user/kakao/callback", produces = "application/json; charset=utf-8")
    public ResponseEntity getLogin(@RequestParam String code, HttpServletResponse httpServletResponse) throws JsonProcessingException {
        Response response = new Response();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));

        Member member = kakaoLoginService.login(code, httpServletResponse);
        SocialLoginResponse kakaoMember = getSocialLoginResponse(member);

        ResponseResource responseResource = new ResponseResource(kakaoMember);
        responseResource.add(linkTo(SocialLoginController.class).slash("user/kakao/callback").withSelfRel());
        response.setStatus(StatusEnum.OK);
        response.setMessage("로그인 완료");
        response.setData(responseResource);
        return new ResponseEntity<>(response, headers, HttpStatus.OK);
    }

    @GetMapping(path = "/user/naver/callback")
    public ResponseEntity naverLogin(@RequestParam("code") String code, @RequestParam("state") String state, HttpServletResponse httpServletResponse)throws JsonProcessingException {
        Response response = new Response();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));

        Member member = naverLoginService.naverLogin(code, state, httpServletResponse);
        SocialLoginResponse naverMember = getSocialLoginResponse(member);

        ResponseResource responseResource = new ResponseResource(naverMember);
        responseResource.add(linkTo(SocialLoginController.class).slash("user/member/callback").withSelfRel());
        response.setStatus(StatusEnum.OK);
        response.setMessage("로그인 완료");
        response.setData(responseResource);
        return new ResponseEntity<>(response, headers, HttpStatus.OK);

    }

    private static SocialLoginResponse getSocialLoginResponse(Member member) {

        SocialLoginResponse kakaoMember = SocialLoginResponse.builder()
                .email(member.getEmail())
                .nickname(member.getNickname())
                .build();
        return kakaoMember;
    }

}
