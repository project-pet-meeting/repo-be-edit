package sideproject.petmeeting.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationExtension;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static sideproject.petmeeting.member.domain.UserRole.ROLE_MEMBER;

@ExtendWith({SpringExtension.class, RestDocumentationExtension.class})
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Slf4j
@Transactional
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

    @BeforeEach
    void init() {
        Member member = Member.builder()
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
    @DisplayName("정상적인 채팅방 생성")
    public void createChatRoom() throws Exception {
        ChatRoomRequestDto chatRoomRequestDto = new ChatRoomRequestDto("test room");

        Post post = postRepository.findByTitle("post").get();

        this.mockMvc.perform(post("/api/chat/" + post.getId())
                        .header("Authorization", getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(HAL_JSON)
                        .content(objectMapper.writeValueAsString(chatRoomRequestDto))
                )
                .andExpect(status().isCreated());

        Member member = memberRepository.findByEmail("test@test.com").get();
        ChatRoom chatRoom = chatRoomRepository.findByPost(post).get();
        ChatMember chatMember = chatMemberRepository.findById(1L).get();
        Long memberId = member.getId();

        assertAll(
                () -> assertThat(chatRoomRepository.findAll().size()).isEqualTo(1),
                () -> assertThat(chatRoom.getRoomName()).isEqualTo("test room"),
                () -> assertThat(chatRoom.getPost().getMember().getId()).isEqualTo(memberId),
                () -> assertThat(chatMemberRepository.findAll().size()).isEqualTo(1),
                () -> assertThat(chatMember.getMember()).isEqualTo(member),
                () -> assertThat(chatMember.getChatRoom()).isEqualTo(chatRoom),
                ()-> assertThat(chatMember.getMember()).isEqualTo(post.getMember())
        );
    }

    @Test
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
    @DisplayName("채팅방 정보 조회")
    public void getChatRoomList() throws Exception {

        // Given
        Post post = Post.builder()
                .title("post2")
                .content("content")
                .imageUrl("test-test.com")
                .member(memberRepository.findByEmail("test@test.com").get())
                .build();
        Post savedPost = postRepository.save(post);
        Post findPost = postRepository.findByTitle("post").get();
        ChatRoom chatRoom1 = ChatRoom.builder()
                .post(findPost)
                .roomName("first chat room")
                .build();
        ChatRoom chatRoom2 = ChatRoom.builder()
                .post(savedPost)
                .roomName("second chat room")
                .build();
        chatRoomRepository.save(chatRoom1);
        chatRoomRepository.save(chatRoom2);

        this.mockMvc.perform(get("/api/chat")
                        .header("Authorization", getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(HAL_JSON))
                .andExpect(status().isOk())
                .andDo(print());
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