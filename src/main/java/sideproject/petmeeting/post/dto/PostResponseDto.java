package sideproject.petmeeting.post.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import sideproject.petmeeting.post.domain.Category;

import java.time.LocalDateTime;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PostResponseDto {
    private Long id;
    private Category category;
    private String title;
    private String content;
    private String imageUrl;
    @ColumnDefault("0")
    private Integer numHeart;
    private Long authorId;
    private String authorNickname;
    private String authorImageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}
