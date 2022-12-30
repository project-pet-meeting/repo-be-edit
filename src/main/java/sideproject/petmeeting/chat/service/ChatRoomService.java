package sideproject.petmeeting.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sideproject.petmeeting.chat.domain.ChatMember;
import sideproject.petmeeting.chat.domain.ChatRoom;
import sideproject.petmeeting.chat.domain.RedisChatRoom;
import sideproject.petmeeting.chat.dto.request.ChatRoomRequestDto;
import sideproject.petmeeting.chat.dto.response.ChatRoomResponseDto;
import sideproject.petmeeting.chat.repository.ChatMemberRepository;
import sideproject.petmeeting.chat.repository.ChatRoomRepository;
import sideproject.petmeeting.chat.repository.RedisChatRoomRepository;
import sideproject.petmeeting.common.exception.BusinessException;
import sideproject.petmeeting.common.exception.ErrorCode;
import sideproject.petmeeting.meeting.domain.Meeting;
import sideproject.petmeeting.meeting.repository.MeetingRepository;
import sideproject.petmeeting.member.domain.Member;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final MeetingRepository meetingRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMemberRepository chatMemberRepository;
    private final RedisChatRoomRepository redisChatRoomRepository;

    @Transactional
    public ChatRoom createChatRoom(Member member, Long meetingId, ChatRoomRequestDto chatRoomRequestDto) {
        Meeting meeting = meetingRepository.findById(meetingId).orElseThrow(
                () -> new BusinessException("존재하지 않는 게시글 id 입니다.", ErrorCode.POST_NOT_EXIST)
        );

        // pub/sub 을 위한 redis 채팅방 생성
        RedisChatRoom redisChatRoom = redisChatRoomRepository.createChatRoom(chatRoomRequestDto.getName(), String.valueOf(meetingId));
        ChatRoom chatRoom = ChatRoom.builder()
                .meeting(meeting)
                .roomId(redisChatRoom.getRoomId())
                .roomName(redisChatRoom.getName())
                .build();
        // 게시글 작성자는 채팅방에 자동 가입
        ChatMember chatMember = ChatMember.builder()
                .chatRoom(chatRoom)
                .member(member)
                .build();

        ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);
        chatMemberRepository.save(chatMember);
        return savedChatRoom;
    }

    public List<ChatRoomResponseDto> getChatRoomList() {
        List<ChatRoom> chatRoomList = chatRoomRepository.findAll();
        List<ChatRoomResponseDto> chatRoomResponseDtoList = new ArrayList<>();
        for (ChatRoom chatRoom : chatRoomList) {
            chatRoomResponseDtoList.add(
                    ChatRoomResponseDto.builder()
                            .id(chatRoom.getId())
                            .meetingId(chatRoom.getMeeting().getId())
                            .roomName(chatRoom.getRoomName())
                            .build()
            );
        }
        return chatRoomResponseDtoList;
    }
}
