package sideproject.petmeeting.myPage.dto;

import lombok.Builder;
import lombok.Getter;
import sideproject.petmeeting.meeting.dto.MeetingResponseDto;

import java.util.List;

@Getter
@Builder
public class MyMeetingDto {
    List<MeetingResponseDto> myMeetingList;
}
