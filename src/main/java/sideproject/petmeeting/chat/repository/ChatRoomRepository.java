package sideproject.petmeeting.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sideproject.petmeeting.chat.domain.ChatRoom;
import sideproject.petmeeting.meeting.domain.Meeting;

import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findByMeeting(Meeting meeting);
}
