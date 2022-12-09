package sideproject.petmeeting.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sideproject.petmeeting.chat.domain.ChatRoom;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

}
