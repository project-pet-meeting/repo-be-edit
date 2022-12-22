package sideproject.petmeeting.post.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sideproject.petmeeting.post.domain.Post;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    // 게시글 전체 조회(수정된 시간으로 내림차순, 페이지 처리)
    Page<Post> findAllByOrderByModifiedAtDesc(Pageable pageable);

    // 게시글 단건 조회
    @Query("select p from Post p left join fetch p.member where p.id = :postId")
    Optional<Post> findPostFetchJoin(@Param("postId")Long postId);

    // 마이페이지 '좋아요'한 게시글 조회
    List<Post> findAllByMemberId(Long id);

}
