package sideproject.petmeeting.post.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sideproject.petmeeting.post.domain.Category;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotEmpty;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PostRequestDto {

    @Enumerated(EnumType.STRING)
    private Category category;

    @NotEmpty(message = "제목을 입력해 주세요.")
    private String title;

    @NotEmpty(message = "내용을 입력해 주세요.")
    private String content;
}
