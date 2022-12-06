package sideproject.petmeeting.social.naver;

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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NaverLoginService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;

    @Value("${spring.security.oauth2.client.registration.naver.client-id}")
    private String clientId;
    @Value("${spring.security.oauth2.client.registration.naver.client-secret}")
    private String clientSecret;

    public Member naverLogin(String code, String state, HttpServletResponse httpServletResponse) throws JsonProcessingException {

        // 1. 받은 code 와 state로 AccessToken 받기
        String accessToken = getAccessToken(code, state);

        // 2. accesstoken으로 유저정보받기
        NaverMemberInfo memberInfo = getNaverMemberInfo(accessToken);

        // 3. 회원 가입이 되지 않은 경우 회원 가입 진행
        Member member = registerMember(memberInfo);

        // 4. 강제 로그인 처리
        forceLogin(httpServletResponse, member);

        return member;
    }

    private String getAccessToken(String code, String state) throws JsonProcessingException {

        HttpHeaders accessTokenHeaders = new HttpHeaders();
        accessTokenHeaders.add("Content-type", "application/x-www-form-urlencoded");

        MultiValueMap<String, String> accessTokenParams = new LinkedMultiValueMap<>();
        accessTokenParams.add("grant_type", "authorization_code");
        accessTokenParams.add("client_id", clientId);
        accessTokenParams.add("client_secret", clientSecret);
        accessTokenParams.add("state", state);
        accessTokenParams.add("code", code);    // 응답으로 받은 코드

        HttpEntity<MultiValueMap<String, String>> accessTokenRequest = new HttpEntity<>(accessTokenParams, accessTokenHeaders);
        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> accessTokenResponse = rt.exchange(
                "https://nid.naver.com/oauth2.0/token",
                HttpMethod.POST,
                accessTokenRequest,
                String.class
        );


        // HTTP 응답 (JSON) -> 액세스 토큰 파싱
        String responseBody = accessTokenResponse.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
//        log.info(jsonNode.toPrettyString());
        return jsonNode.get("access_token").asText();
    }

    private NaverMemberInfo getNaverMemberInfo(String accessToken) throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HTTP 요청 보내기
        HttpEntity<MultiValueMap<String, String>> naverMemberInfoRequest = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(
                "https://openapi.naver.com/v1/nid/me",
                HttpMethod.GET,
                naverMemberInfoRequest,
                String.class
        );

        String responseBody = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        System.out.println(jsonNode.toString());

        String email = jsonNode.get("response").get("email").asText();
        String imgUrl = jsonNode.get("response").get("profile_image").asText();
        String nickname = jsonNode.get("response").get("nickname").asText();


        return NaverMemberInfo.builder()
                .email(email)
                .imageUrl(imgUrl)
                .nickname(nickname)
                .build();
    }

    @Transactional
    Member registerMember(NaverMemberInfo memberInfo) {
        String email = memberInfo.getEmail();
        Member member = memberRepository.findByEmail(email).orElse(null);
        if (member == null) {
            member = Member.builder()
                    .email(memberInfo.getEmail())
                    .nickname(memberInfo.getNickname())
                    .image(memberInfo.getImageUrl())
                    .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                    .userRole(ROLE_MEMBER)
                    .build();
            memberRepository.save(member);
        }
        return member;
    }
    private void forceLogin(HttpServletResponse httpServletResponse, Member member) {
        TokenDto tokenDto = tokenProvider.generateTokenDto(member);
        httpServletResponse.addHeader("Authorization", "Bearer " + tokenDto.getAccessToken());
        httpServletResponse.addHeader("RefreshToken", tokenDto.getRefreshToken());

        UserDetailsImpl userDetails = new UserDetailsImpl(member);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, tokenDto.getAccessToken(), userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
