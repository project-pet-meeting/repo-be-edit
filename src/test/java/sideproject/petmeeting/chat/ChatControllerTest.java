package sideproject.petmeeting.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import sideproject.petmeeting.chat.domain.ChatMember;
import sideproject.petmeeting.chat.domain.ChatRoom;
import sideproject.petmeeting.chat.dto.request.ChatRoomRequestDto;
import sideproject.petmeeting.chat.repository.ChatMemberRepository;
import sideproject.petmeeting.chat.repository.ChatRoomRepository;
import sideproject.petmeeting.member.domain.Member;
import sideproject.petmeeting.member.dto.request.LoginRequestDto;
import sideproject.petmeeting.member.repository.MemberRepository;
import sideproject.petmeeting.post.domain.Post;
import sideproject.petmeeting.post.repository.PostRepository;
import sideproject.petmeeting.token.repository.RefreshTokenRepository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.hateoas.MediaTypes.HAL_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static sideproject.petmeeting.member.domain.UserRole.ROLE_MEMBER;

@ExtendWith({SpringExtension.class, RestDocumentationExtension.class})
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@TestMethodOrder(value = MethodOrderer.OrderAnnotation.class)
@Rollback
class ChatControllerTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    RefreshTokenRepository refreshTokenRepository;
    @Autowired
    PostRepository postRepository;
    @Autowired
    ChatRoomRepository chatRoomRepository;
    @Autowired
    ChatMemberRepository chatMemberRepository;

    @Order(1)
    @Test
    @DisplayName("기본 Entity 생성")
    public void setup() {

        Member member = Member.builder()
                .id(1L)
                .nickname("Tommy")
                .password("test")
                .email("test@test.com")
                .image("test-image")
                .userRole(ROLE_MEMBER)
                .build();
        Member savedMember = memberRepository.save(member);
        Post post = Post.builder()
                .title("post")
                .content("content")
                .imageUrl("test-test.com")
                .member(savedMember)
                .build();
        postRepository.save(post);
    }

    @Test
    @Order(2)
    @DisplayName("정상적인 채팅방 생성")
    @Transactional
    public void createChatRoom() throws Exception {
        ChatRoomRequestDto chatRoomRequestDto = new ChatRoomRequestDto("test room");

        this.mockMvc.perform(post("/api/chat/1")
                        .header("Authorization", getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(HAL_JSON)
                        .content(objectMapper.writeValueAsString(chatRoomRequestDto))
                )
                .andExpect(status().isCreated());

        Member member = memberRepository.findById(1L).get();
        ChatRoom chatRoom = chatRoomRepository.findById(1L).get();
        ChatMember chatMember = chatMemberRepository.findById(1L).get();
        Post post = postRepository.findById(1L).get();

        assertAll(
                () -> assertThat(chatRoomRepository.findAll().size()).isEqualTo(1),
                () -> assertThat(chatRoom.getRoomName()).isEqualTo("test room"),
                () -> assertThat(chatRoom.getPost().getMember().getId()).isEqualTo(1L),
                () -> assertThat(chatMemberRepository.findAll().size()).isEqualTo(1),
                () -> assertThat(chatMember.getMember()).isEqualTo(member),
                () -> assertThat(chatMember.getChatRoom()).isEqualTo(chatRoom),
                ()-> assertThat(chatMember.getMember()).isEqualTo(post.getMember())
        );
    }

    @Test
    @Order(3)
    @DisplayName("게시글이 없을 경우 Error 처리")
    public void createChatRoom_BadRequest() throws Exception {
        ChatRoomRequestDto chatRoomRequestDto = new ChatRoomRequestDto("test room");

        this.mockMvc.perform(post("/api/chat/10")
                        .header("Authorization", getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(HAL_JSON)
                        .content(objectMapper.writeValueAsString(chatRoomRequestDto))
                )
                .andExpect(status().isNotFound());
    }

    @Test
    private String getAccessToken() throws Exception {
        // Given
        String email = "test@test.com";
        String password = "test";
        LoginRequestDto loginRequestDto = new LoginRequestDto(email, password);

        // When & Then
        ResultActions perform = this.mockMvc.perform(post("/api/member/login")
                        .contentType(APPLICATION_JSON)
                        .accept(HAL_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDto)))
                .andDo(print())
                .andExpect(status().isOk());
        assertThat(refreshTokenRepository.findAll().size()).isEqualTo(1);

        return perform.andReturn().getResponse().getHeader("Authorization");
    }
}