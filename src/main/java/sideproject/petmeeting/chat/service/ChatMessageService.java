package sideproject.petmeeting.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sideproject.petmeeting.chat.domain.ChatMessage;
import sideproject.petmeeting.chat.dto.response.ChatMessageResponseDto;
import sideproject.petmeeting.chat.repository.ChatMessageRepository;
import sideproject.petmeeting.common.exception.BusinessException;
import sideproject.petmeeting.common.exception.ErrorCode;
import sideproject.petmeeting.member.domain.Member;
import sideproject.petmeeting.member.repository.MemberRepository;

import java.util.ArrayList;
import java.util.List;

import static sideproject.petmeeting.common.exception.ErrorCode.MEMBER_NOT_EXIST;

@Service
@RequiredArgsConstructor
public class ChatMessageService {
    private final ChatMessageRepository chatMessageRepository;
    private final MemberRepository memberRepository;

    public List<ChatMessageResponseDto> getMessageList(String chatRoomId) {
        List<ChatMessage> chatMessageList = chatMessageRepository.findByRoomId(chatRoomId);

        List<ChatMessageResponseDto> messageResponseList = new ArrayList<>();
        for (ChatMessage chatMessage : chatMessageList) {
            Member chatMember = memberRepository.findByNickname(chatMessage.getSender()).orElseThrow(
                    () -> new BusinessException("적절하지 않은 접근 경로 입니다.", MEMBER_NOT_EXIST)
            );
            messageResponseList.add(
                    ChatMessageResponseDto.builder()
                            .id(chatMessage.getId())
                            .type(chatMessage.getType())
                            .roomId(chatMessage.getRoomId())
                            .sender(chatMessage.getSender())
                            .senderImage(chatMember.getImage())
                            .message(chatMessage.getMessage())
                            .build()
            );
        }
        return messageResponseList;
    }
}
