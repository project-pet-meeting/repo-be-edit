package sideproject.petmeeting.meeting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sideproject.petmeeting.meeting.domain.Meeting;

import java.util.List;
import java.util.Optional;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long> {

    @Query("select mt from Meeting mt left join fetch mt.member where mt.id = :meetingId")
    Optional<Meeting> findMeetingIdFetchJoin(@Param("meetingId")Long meetingId);

    List<Meeting> findAllByMemberId(Long id);

    Optional<Meeting> findByTitle(String title);

}
