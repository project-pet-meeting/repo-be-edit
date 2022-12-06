package sideproject.petmeeting.social.kakao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import sideproject.petmeeting.member.domain.Member;
import sideproject.petmeeting.member.repository.MemberRepository;
import sideproject.petmeeting.security.TokenProvider;
import sideproject.petmeeting.security.UserDetailsImpl;
import sideproject.petmeeting.token.dto.TokenDto;

import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

import static sideproject.petmeeting.member.domain.UserRole.ROLE_MEMBER;

@Service
@RequiredArgsConstructor
@Slf4j
public class KakaoLoginService {

    @Value("${kakao.clientId}")
    private String clientId;

    @Value(("${kakao.redirect.uri}"))
    private String redirectUri;
    private final MemberRepository memberRepository;
    private final TokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;

    public Member login(String code, HttpServletResponse httpServletResponse) throws JsonProcessingException {

        // 1. "인가 코드" 로 "액세스 토큰" 요청
        String accessToken = getAccessToken(code);

        // 2. "액세스 토큰"으로 "카카오 사용자 정보" 가져오기
        KakaoUserInfo userInfo = getUserInfo(accessToken);

        // 3. 이메일 중복 검사
        String email = userInfo.getEmail();
        Member member = memberRepository.findByEmail(email).orElse(null);
        if (member == null) {
            Member kakaoMember = buildMember(userInfo);
            memberRepository.save(kakaoMember);
        }

        // 4. 강제 로그인 처리
        forceLogin(member);

        // 5. 토큰 생성
        generateToken(httpServletResponse, member);

        return memberRepository.findByEmail(email).get();
    }

    private static KakaoUserInfo getUserInfo(String accessToken) throws JsonProcessingException {
        // HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HTTP 요청 보내기
        HttpEntity<MultiValueMap<String, String>> kakaoUserInfoRequest = new HttpEntity<>(headers);
        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> response = rt.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.POST,
                kakaoUserInfoRequest,
                String.class
        );

        String responseBody = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);

        String email = String.valueOf(jsonNode.get("kakao_account").get("email"));
        String nickname = String.valueOf(jsonNode.get("kakao_account").get("profile").get("nickname"));
        String imageUrl = String.valueOf(jsonNode.get("properties").get("profile_image"));

        return new KakaoUserInfo(nickname, imageUrl, email);
    }

    private String getAccessToken(String code) throws JsonProcessingException {
        // HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HTTP Body 생성
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", clientId);
        body.add("redirect_uri", redirectUri);
        body.add("code", code);

        // HTTP 요청 보내기
        HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest =
                new HttpEntity<>(body, headers);
        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> response = rt.exchange(
                "https://kauth.kakao.com/oauth/token",
                HttpMethod.POST,
                kakaoTokenRequest,
                String.class
        );

        // HTTP 응답 (JSON) -> 액세스 토큰 파싱
        String responseBody = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        return jsonNode.get("access_token").asText();
    }

    private Member buildMember(KakaoUserInfo userInfo) {
        String nickname = userInfo.getNickname();
        if (nickname == null) {
            nickname = "kakao" + UUID.randomUUID().toString();
        }
        Member kakaoMember = Member.builder()
                .nickname(nickname)
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .email(userInfo.getEmail())
                .image(userInfo.getImgUrl())
                .userRole(ROLE_MEMBER)
                .build();
        return kakaoMember;
    }

    private static void forceLogin(Member member) {
        UserDetailsImpl userDetails = new UserDetailsImpl(member);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private void generateToken(HttpServletResponse httpServletResponse, Member member) {
        TokenDto tokenDto = tokenProvider.generateTokenDto(member);
        httpServletResponse.addHeader("Authorization", "Bearer " + tokenDto.getAccessToken());
        httpServletResponse.addHeader("RefreshToken", tokenDto.getRefreshToken());
    }
}
