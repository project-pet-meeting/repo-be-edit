package sideproject.petmeeting.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import sideproject.petmeeting.member.domain.Member;
import sideproject.petmeeting.post.domain.HeartPost;
import sideproject.petmeeting.post.domain.Post;

import java.util.Optional;

public interface HeartPostRepository extends JpaRepository<HeartPost, Long> {

    // 좋아요 여부 확인
    Optional<HeartPost> findByPostAndMember(Post post, Member member);

    // Heart Count 구하기
    @Query(value = "SELECT COUNT(h.id) FROM HeartPost h WHERE h.post.id = :PostId")
    int findCountHeart(Long PostId);

}
