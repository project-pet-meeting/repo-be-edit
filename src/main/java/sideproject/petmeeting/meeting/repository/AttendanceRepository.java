package sideproject.petmeeting.meeting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sideproject.petmeeting.meeting.domain.Attendance;
import sideproject.petmeeting.meeting.domain.Meeting;
import sideproject.petmeeting.member.domain.Member;

import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long>  {

    // 모임 참여 여부 확인
    Optional<Attendance> findByMeetingAndMember(@Param("meeting") Meeting meeting, @Param("member") Member member);

    // 모임 참석자 조회
    @Query("select a from Attendance a left join fetch a.member where a.meeting.id = :meetingId")
    List<Attendance> findMemberFetchJoin(@Param("meetingId")Long meetingId);

    // 모임 참여 인원 카운트
    Integer countByMeetingId(@Param("meetingId")Long meetingId);
}
