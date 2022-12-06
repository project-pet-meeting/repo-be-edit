package sideproject.petmeeting.social;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
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

import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequiredArgsConstructor
public class SocialLoginController {
    private final KakaoLoginService kakaoLoginService;

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

    private static SocialLoginResponse getSocialLoginResponse(Member member) {
        SocialLoginResponse kakaoMember = SocialLoginResponse.builder()
                .email(member.getEmail())
                .nickname(member.getNickname())
                .build();
        return kakaoMember;
    }

}
