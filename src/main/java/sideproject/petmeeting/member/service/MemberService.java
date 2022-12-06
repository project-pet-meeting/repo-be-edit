package sideproject.petmeeting.member.service;

import lombok.AllArgsConstructor;
import net.bytebuddy.pool.TypePool;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import sideproject.petmeeting.common.S3Uploader;
import sideproject.petmeeting.member.domain.Member;
import sideproject.petmeeting.member.domain.UserRole;
import sideproject.petmeeting.member.dto.request.LoginRequestDto;
import sideproject.petmeeting.member.dto.request.MemberDto;
import sideproject.petmeeting.member.dto.request.MemberUpdateRequest;
import sideproject.petmeeting.member.repository.MemberRepository;
import sideproject.petmeeting.security.TokenProvider;
import sideproject.petmeeting.token.domain.RefreshToken;
import sideproject.petmeeting.token.dto.TokenDto;
import sideproject.petmeeting.token.repository.RefreshTokenRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

import static sideproject.petmeeting.member.domain.UserRole.ROLE_MEMBER;

@Service
@AllArgsConstructor
public class MemberService {

    //== Dependency Injection ==//
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final S3Uploader s3Uploader;

    public Member join(MemberDto memberDto, MultipartFile image) throws ResponseStatusException, IOException {

        Optional<Member> optionalMember = memberRepository.findByEmail(memberDto.getEmail());
        if (optionalMember.isPresent()) {
            throw new IllegalStateException("이미 회원이 존재합니다.");
        }
        String imageUrl;
        if (image == null) {
            imageUrl = "https://kimsky.s3.ap-northeast-2.amazonaws.com/member/image/%E1%84%92%E1%85%AC%E1%84%8B%E1%85%AF%E1%86%AB%E1%84%80%E1%85%B5%E1%84%87%E1%85%A9%E1%86%AB%E1%84%8B%E1%85%B5%E1%84%86%E1%85%B5%E1%84%8C%E1%85%B5.jpeg";
        } else {
            imageUrl = s3Uploader.upload(image, "/member/image");
        }


        Member member = Member.builder()
                .nickname(memberDto.getNickname())
                .password(passwordEncoder.encode(memberDto.getPassword()))
                .email(memberDto.getEmail())
                .image(imageUrl)
                .userRole(ROLE_MEMBER)
                .build();

        Member savedMember = memberRepository.save(member);
        return savedMember;

    }
    @Transactional
    public void login(LoginRequestDto loginRequestDto, HttpServletResponse httpServletResponse) {
        Optional<Member> optionalMember = memberRepository.findByEmail(loginRequestDto.getEmail());
        if (optionalMember.isEmpty()) {
            throw new IllegalStateException("잘못된 아이디 입니다.");
        }
        TokenDto tokenDto = tokenProvider.generateTokenDto(optionalMember.get());
        httpServletResponse.addHeader("Authorization", "Bearer " + tokenDto.getAccessToken());
        httpServletResponse.addHeader("RefreshToken", tokenDto.getRefreshToken());
    }

    @Transactional
    public Member update(MemberUpdateRequest memberUpdateRequest, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        if (httpServletRequest.getHeader("Authorization") == null || httpServletRequest.getHeader("Authorization").isEmpty()) {
            throw new IllegalStateException("잘못된 접근입니다.");
        }
        tokenProvider.validateToken(httpServletRequest.getHeader("Authorization").substring(7));
        String authorization = tokenProvider.getUserEmailByToken(httpServletRequest.getHeader("Authorization"));
        Optional<Member> optionalMember = memberRepository.findByEmail(authorization);
        if (optionalMember.isEmpty()) {
            throw new IllegalStateException("회원이 존재하지 않습니다");
        }
        Member member = optionalMember.get();
        Member updateMember = member.update(memberUpdateRequest);
        TokenDto tokenDto = tokenProvider.generateTokenDto(updateMember);
        httpServletResponse.addHeader("Authorization", "Bearer " + tokenDto.getAccessToken());
        httpServletResponse.addHeader("RefreshToken", tokenDto.getRefreshToken());
        return updateMember;
    }

    public void logout(HttpServletRequest httpServletRequest) {
        if (httpServletRequest.getHeader("Authorization") == null || httpServletRequest.getHeader("Authorization").isEmpty()) {
            throw new IllegalStateException("잘못된 접근입니다.");
        }
        tokenProvider.validateToken(httpServletRequest.getHeader("Authorization").substring(7));
        String authorization = tokenProvider.getUserEmailByToken(httpServletRequest.getHeader("Authorization"));
        Optional<Member> optionalMember = memberRepository.findByEmail(authorization);
        if (optionalMember.isEmpty()) {
            throw new IllegalStateException("회원이 존재하지 않습니다");
        }
        Member member = optionalMember.get();
        RefreshToken refreshToken = refreshTokenRepository.findByMember(member).get();
        refreshTokenRepository.delete(refreshToken);
    }
}
