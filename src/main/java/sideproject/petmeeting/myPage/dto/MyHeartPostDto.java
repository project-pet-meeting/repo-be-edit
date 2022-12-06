package sideproject.petmeeting.myPage.dto;

import lombok.Builder;
import lombok.Getter;
import sideproject.petmeeting.post.dto.PostResponseDto;

import java.util.List;

@Getter
@Builder
public class MyHeartPostDto {
    List<PostResponseDto> myHeartPostList;
}
