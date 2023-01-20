package sideproject.petmeeting.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sideproject.petmeeting.chat.domain.ChatMessage;
import sideproject.petmeeting.chat.repository.ChatMessageRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMessageService {
    private final ChatMessageRepository chatMessageRepository;

    public List<ChatMessage> getMessageList(String chatRoomId) {
        List<ChatMessage> chatMessageList = chatMessageRepository.findByRoomId(chatRoomId);
        return chatMessageList;
    }
}
