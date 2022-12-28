package sideproject.petmeeting.meeting.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sideproject.petmeeting.member.domain.Member;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
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
    private int currentNum;
    private String species;
    private Long authorId;
    private String authorNickname;
    private String authorImageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private List<Member> memberList;
}
