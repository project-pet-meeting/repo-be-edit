package sideproject.petmeeting.comment.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import sideproject.petmeeting.post.domain.Post;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ActiveProfiles("test")
class CommentTest {

    @Test
    @DisplayName("Comment 가 제대로 생성 되는지 체크 ")
    void createComment() {
        Post post= Post.builder()
                .title("test title")
                .build();
        Comment comment = Comment.builder()
                .post(post)
                .content("Test content")
                .build();
        assertThat(comment).isNotNull();
    }
}