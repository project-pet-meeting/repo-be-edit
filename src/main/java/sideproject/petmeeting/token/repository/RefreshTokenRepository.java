package sideproject.petmeeting.token.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sideproject.petmeeting.member.domain.Member;
import sideproject.petmeeting.token.domain.RefreshToken;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {

    Optional<RefreshToken> findByMember(Member member);
}
