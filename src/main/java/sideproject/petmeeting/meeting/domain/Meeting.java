package sideproject.petmeeting.meeting.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sideproject.petmeeting.common.Timestamped;
import sideproject.petmeeting.meeting.dto.MeetingRequestDto;
import sideproject.petmeeting.member.domain.Member;

import javax.persistence.*;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

import static javax.persistence.FetchType.LAZY;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Meeting extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty
    private String title;

    @NotEmpty
    private String content;

    @Size(max = 2000)
    private String imageUrl;

    @NotEmpty
    private String address;

    @NotEmpty
    private String coordinateX;

    @NotEmpty
    private String coordinateY;

    @NotEmpty
    private String placeName;

    @FutureOrPresent
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime time;

    @NotNull
    private int recruitNum;

    @NotEmpty
    private String species;

    @JsonIgnore
    @JoinColumn(nullable = false)
    @ManyToOne(fetch = LAZY)
    private Member member;


    public void update(MeetingRequestDto meetingRequestDto, String imageUrl) {
        this.title = meetingRequestDto.getTitle();
        this.content = meetingRequestDto.getContent();
        this.imageUrl = imageUrl;
        this.address = meetingRequestDto.getAddress();
        this.coordinateX = meetingRequestDto.getCoordinateX();
        this.coordinateY = meetingRequestDto.getCoordinateY();
        this.placeName = meetingRequestDto.getPlaceName();
        this.time = meetingRequestDto.getTime();
        this.recruitNum = meetingRequestDto.getRecruitNum();
        this.species = meetingRequestDto.getSpecies();
    }


}
