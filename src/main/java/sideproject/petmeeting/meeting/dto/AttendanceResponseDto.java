package sideproject.petmeeting.meeting.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AttendanceResponseDto {
    private Long id;
    private String nickname;
    private String image;
    private String location;
    private List<AttendanceResponseDto> attendanceList;
}
