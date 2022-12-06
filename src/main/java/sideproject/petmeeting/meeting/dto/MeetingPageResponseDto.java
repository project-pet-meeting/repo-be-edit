package sideproject.petmeeting.meeting.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class MeetingPageResponseDto {
    List<MeetingResponseDto> meetingList;
    private Integer totalPage;
    private Integer currentPage;
    private Long totalPost;
    private boolean isFirstPage;
    private boolean hasNextPage;
    private boolean hasPreviousPage;
}
