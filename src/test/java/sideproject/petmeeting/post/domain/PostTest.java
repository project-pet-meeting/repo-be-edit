package sideproject.petmeeting.post.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import sideproject.petmeeting.chat.domain.ChatMember;
import sideproject.petmeeting.chat.repository.ChatMemberRepository;
import sideproject.petmeeting.chat.domain.ChatRoom;
import sideproject.petmeeting.chat.repository.ChatRoomRepository;
import sideproject.petmeeting.member.domain.Member;
import sideproject.petmeeting.member.repository.MemberRepository;
import sideproject.petmeeting.post.repository.PostRepository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static sideproject.petmeeting.member.domain.UserRole.ROLE_MEMBER;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class PostTest {

    @Autowired
    PostRepository postRepository;
    @Autowired
    ChatRoomRepository chatRoomRepository;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    ChatMemberRepository chatMemberRepository;

    @Test
    @DisplayName("게시글 - 채팅방 - 채팅 회원간 SQL 이 제대로 실행 되는 지 확인")
    @Transactional
    public void checkCascade() {
        Member member = Member.builder()
                .nickname("Tommy")
                .password("test")
                .email("test@test.com")
                .image("test-image")
                .userRole(ROLE_MEMBER)
                .build();
        Member savedMember = memberRepository.save(member);
        ChatRoom chatRoom = ChatRoom.builder()
                .roomName("chatRoom")
                .build();
        ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);
        Post post = Post.builder()
                .title("post")
                .content("content")
                .imageUrl("test-test.com")
                .member(savedMember)
//                .chatRoom(chatRoom)
                .build();
        Post savedPost = postRepository.save(post);
        ChatMember chatMember = ChatMember.builder()
                .chatRoom(savedChatRoom)
                .member(member)
                .build();
        chatMemberRepository.save(chatMember);

        assertAll(
                () -> assertThat(memberRepository.findAll().size()).isEqualTo(1),
                () -> assertThat(postRepository.findAll().size()).isEqualTo(1),
                () -> assertThat(chatRoomRepository.findAll().size()).isEqualTo(1),
                () -> assertThat(chatMemberRepository.findAll().size()).isEqualTo(1)
//                () -> assertThat(savedPost.getChatRoom()).isEqualTo(savedChatRoom)
        );
        chatMemberRepository.deleteAllByChatRoom(chatRoom);
        postRepository.delete(savedPost);
        assertAll(
                () -> assertThat(postRepository.findAll().size()).isEqualTo(0),
                () -> assertThat(chatRoomRepository.findAll().size()).isEqualTo(0),
                () -> assertThat(chatMemberRepository.findAll().size()).isEqualTo(0)
        );
    }
}