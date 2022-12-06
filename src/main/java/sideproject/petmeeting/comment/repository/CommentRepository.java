package sideproject.petmeeting.comment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sideproject.petmeeting.comment.domain.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findAllByPostId(Long postId);
}
