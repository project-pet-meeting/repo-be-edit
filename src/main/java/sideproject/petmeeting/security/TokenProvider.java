package sideproject.petmeeting.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import sideproject.petmeeting.member.domain.Member;
import sideproject.petmeeting.token.domain.RefreshToken;
import sideproject.petmeeting.token.dto.TokenDto;
import sideproject.petmeeting.token.repository.RefreshTokenRepository;

import java.security.Key;
import java.util.Date;
import java.util.Optional;

import static io.jsonwebtoken.SignatureAlgorithm.HS256;

@Component
@Slf4j
public class TokenProvider {

    private static final String AUTHORITIES_KEY = "auth";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 30 * 6;            //30분
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24 * 7;     //7일
    private final Key key;
    private final RefreshTokenRepository refreshTokenRepository;

    // 암호화
    public TokenProvider(@Value("${jwt.secret}") String secretKey,
                         RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    // 토큰 생성
    @Transactional
    public TokenDto generateTokenDto(Member member) {
        Optional<RefreshToken> optionalRefreshToken = refreshTokenRepository.findByMember(member);
        if (!optionalRefreshToken.isEmpty()) {
            refreshTokenRepository.delete(optionalRefreshToken.get());
        }
        long now = new Date().getTime();

        Date accessTokenExpiresIn = new Date(now + ACCESS_TOKEN_EXPIRE_TIME);
        String accessToken = Jwts.builder()
                .setSubject(member.getEmail())
                .claim(AUTHORITIES_KEY, member.getUserRole().toString())
                .setExpiration(accessTokenExpiresIn)
                .signWith(key, HS256)
                .compact();

        String refreshToken = Jwts.builder()
                .setExpiration(new Date(now + REFRESH_TOKEN_EXPIRE_TIME))
                .signWith(key, HS256)
                .compact();

        RefreshToken refreshTokenObject = RefreshToken.builder()
                .id(member.getEmail())
                .member(member)
                .keyValue(refreshToken)
                .build();

        refreshTokenRepository.save(refreshTokenObject);

        return TokenDto.builder()
                .grantType(BEARER_PREFIX)
                .accessToken(accessToken)
                .accessTokenExpiresIn(accessTokenExpiresIn.getTime())
                .refreshToken(refreshToken)
                .build();
    }

    public Member getMemberFromAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || AnonymousAuthenticationToken.class.isAssignableFrom(authentication.getClass())) {
            return null;
        }
        return ((UserDetailsImpl) authentication.getPrincipal()).getMember();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.info("Invalid JWT signature, 유효하지 않는 JWT 서명 입니다.");
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT token, 만료된 JWT token 입니다.");
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT token, 지원되지 않는 JWT 토큰 입니다.");
        } catch (IllegalArgumentException e) {
            log.info("JWT claims is empty, 잘못된 JWT 토큰 입니다.");
        }
        return false;
    }

    public String getUserEmailByToken(String accessToken) {
        String token;
        if (!StringUtils.hasText(accessToken) && !accessToken.startsWith("Bearer ")) {
            return null;
        }
        Claims claims;
        try {
            claims = Jwts
                    .parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(accessToken)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.info("만료된 Token 입니다.");
            return null;
        }
        return claims.get("sub").toString();
    }
}
