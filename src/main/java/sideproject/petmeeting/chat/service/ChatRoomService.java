package sideproject.petmeeting.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sideproject.petmeeting.chat.domain.ChatMember;
import sideproject.petmeeting.chat.domain.ChatRoom;
import sideproject.petmeeting.chat.dto.request.ChatRoomRequestDto;
import sideproject.petmeeting.chat.dto.response.ChatRoomResponseDto;
import sideproject.petmeeting.chat.repository.ChatMemberRepository;
import sideproject.petmeeting.chat.repository.ChatRoomRepository;
import sideproject.petmeeting.comment.dto.response.CommentResponseDto;
import sideproject.petmeeting.common.exception.BusinessException;
import sideproject.petmeeting.common.exception.ErrorCode;
import sideproject.petmeeting.member.domain.Member;
import sideproject.petmeeting.post.domain.Post;
import sideproject.petmeeting.post.repository.PostRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final PostRepository postRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMemberRepository chatMemberRepository;

    @Transactional
    public ChatRoom createChatRoom(Member member, Long postId, ChatRoomRequestDto chatRoomRequestDto) {
        Post post = postRepository.findById(postId).orElseThrow(()
                -> new BusinessException("존재하지 않는 게시글 id 입니다.", ErrorCode.POST_NOT_EXIST));
        ChatRoom chatRoom = ChatRoom.builder()
                .post(post)
                .roomName(chatRoomRequestDto.getName())
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
                            .postId(chatRoom.getPost().getId())
                            .roomName(chatRoom.getRoomName())
                            .build()
            );
        }
        return chatRoomResponseDtoList;
    }
}
