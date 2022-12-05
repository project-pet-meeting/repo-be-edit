package sideproject.petmeeting.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sideproject.petmeeting.member.domain.Member;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByNickname(String nickname);
}
