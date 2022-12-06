package sideproject.petmeeting.meeting.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MeetingRequestDto {

    @NotEmpty(message = "제목을 입력해 주세요.")
    private String title;

    @NotEmpty(message = "내용을 입력해 주세요.")
    private String content;

    @NotEmpty(message = "주소를 입력해 주세요.")
    private String address;

    @NotEmpty(message = "X 좌표를 입력해 주세요.")
    private String coordinateX;

    @NotEmpty(message = "Y 좌표를 입력해 주세요.")
    private String coordinateY;

    @NotEmpty(message = "장소명을 입력해 주세요.")
    private String placeName;

    @FutureOrPresent
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime time;

    @NotNull(message = "모집 인원을 입력해 주세요.")
    private int recruitNum;

    @NotEmpty(message = "만날 반려 동물 종을 입력해 주세요.")
    private String species;
}
