package sideproject.petmeeting.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import sideproject.petmeeting.member.domain.Member;
import sideproject.petmeeting.post.domain.HeartPost;
import sideproject.petmeeting.post.domain.Post;

import java.util.List;
import java.util.Optional;

public interface HeartPostRepository extends JpaRepository<HeartPost, Long> {

    // 좋아요 여부 확인
    Optional<HeartPost> findByPostAndMember(@Param("post")Post post, @Param("member") Member member);

    // Heart Count 구하기
    Integer countByPostId(@Param("postId")Long PostId);

    // 마이페이지 '좋아요'한 게시글 조회
    List<HeartPost> findAllByMemberId(Long id);

}
