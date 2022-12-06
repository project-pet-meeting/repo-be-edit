package sideproject.petmeeting.token.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sideproject.petmeeting.token.domain.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {

}
