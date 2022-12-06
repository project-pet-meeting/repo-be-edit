package sideproject.petmeeting.post.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class PostPageResponseDto {
    List<PostResponseDto> postList;
    private Integer totalPage;
    private Integer currentPage;
    private Long totalPost;
    private boolean isFirstPage;
    private boolean hasNextPage;
    private boolean hasPreviousPage;
}
