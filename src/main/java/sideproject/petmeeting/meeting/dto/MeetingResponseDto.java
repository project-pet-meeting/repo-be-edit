package sideproject.petmeeting.meeting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MeetingResponseDto {
    private Long id;
    private String title;
    private String content;
    private String imageUrl;
    private String address;
    private String coordinateX;
    private String coordinateY;
    private String placeName;
    private LocalDateTime time;
    private int recruitNum;
    private String species;
    private Long authorId;
    private String authorNickname;
    private String authorImageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}
