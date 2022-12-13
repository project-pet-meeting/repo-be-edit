package sideproject.petmeeting.post.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static sideproject.petmeeting.post.domain.Category.RECOMMAND;

class PostTest {

    @Test
    @DisplayName("post 생성")
    void createPost() {

        // Given
        Post post= Post.builder()
                .category(Category.RECOMMAND)
                .title("test title")
                .content("test content")
                .imageUrl("test imageUrl")
                .build();

        // Then
        assertThat(post).isNotNull();
    }

    @Test
    @DisplayName("Post 업데이트")
    void updatePost() {

        // Given
        Post post= Post.builder()
                .category(Category.RECOMMAND)
                .title("test Update title")
                .content("test Update content")
                .imageUrl("test Update imageUrl")
                .build();

        // Then
        assertThat(post).isNotNull();
        assertThat(post.getCategory()).isEqualTo(RECOMMAND);
        assertThat(post.getTitle()).isEqualTo("test Update title");
        assertThat(post.getContent()).isEqualTo("test Update content");
        assertThat(post.getImageUrl()).isEqualTo("test Update imageUrl");
    }

}