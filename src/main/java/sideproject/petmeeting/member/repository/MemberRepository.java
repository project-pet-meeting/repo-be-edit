package sideproject.petmeeting.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sideproject.petmeeting.member.domain.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
