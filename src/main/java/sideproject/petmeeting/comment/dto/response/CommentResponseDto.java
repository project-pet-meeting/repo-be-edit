package sideproject.petmeeting.comment.dto.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class CommentResponseDto {
    private Long id;
    private String content;
}
