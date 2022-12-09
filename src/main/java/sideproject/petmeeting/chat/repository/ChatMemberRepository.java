package sideproject.petmeeting.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sideproject.petmeeting.chat.domain.ChatMember;
import sideproject.petmeeting.chat.domain.ChatRoom;

public interface ChatMemberRepository extends JpaRepository<ChatMember, Long> {

    void deleteAllByChatRoom(ChatRoom chatRoom);

}
