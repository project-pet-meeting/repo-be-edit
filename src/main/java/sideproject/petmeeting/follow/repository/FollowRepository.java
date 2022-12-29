package sideproject.petmeeting.follow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sideproject.petmeeting.follow.domain.Follow;
import sideproject.petmeeting.member.domain.Member;

import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    Optional<Follow> findByFollowing(Member following);
    Optional<Follow> findByFollower(Member follower);

    List<Follow> findAllByFollower(Member follower);

    List<Follow> findAllByFollowing(Member following);
}
