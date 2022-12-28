package sideproject.petmeeting.meeting.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sideproject.petmeeting.common.Timestamped;
import sideproject.petmeeting.member.domain.Member;

import javax.persistence.*;

import static javax.persistence.FetchType.LAZY;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Attendance extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @JoinColumn(nullable = false)
    @ManyToOne(fetch = LAZY)
    private Meeting meeting;

    @JsonIgnore
    @JoinColumn(nullable = false)
    @ManyToOne(fetch = LAZY)
    private Member member;

}
