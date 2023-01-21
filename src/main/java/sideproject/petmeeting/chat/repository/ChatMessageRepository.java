package sideproject.petmeeting.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sideproject.petmeeting.chat.domain.ChatMessage;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByRoomId(String roomId);
}
