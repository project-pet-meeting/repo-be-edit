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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import sideproject.petmeeting.chat.domain.ChatMember;
import sideproject.petmeeting.chat.domain.ChatRoom;
import sideproject.petmeeting.chat.domain.RedisChatRoom;
import sideproject.petmeeting.chat.dto.request.ChatRoomRequestDto;
import sideproject.petmeeting.chat.repository.ChatMemberRepository;
import sideproject.petmeeting.chat.repository.ChatRoomRepository;
import sideproject.petmeeting.chat.repository.RedisChatRoomRepository;
import sideproject.petmeeting.meeting.domain.Meeting;
import sideproject.petmeeting.meeting.repository.MeetingRepository;
import sideproject.petmeeting.member.domain.Member;
import sideproject.petmeeting.member.dto.request.LoginRequestDto;
import sideproject.petmeeting.member.repository.MemberRepository;
import sideproject.petmeeting.token.repository.RefreshTokenRepository;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.hateoas.MediaTypes.HAL_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.modifyUris;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
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
    MeetingRepository meetingRepository;
    @Autowired
    ChatRoomRepository chatRoomRepository;
    @Autowired
    ChatMemberRepository chatMemberRepository;
    @Autowired
    RedisChatRoomRepository redisChatRoomRepository;

    @BeforeEach
    void init(WebApplicationContext webApplicationContext,
              RestDocumentationContextProvider restDocumentationContextProvider) {
        Member member = Member.builder()
                .nickname("Tommy")
                .password("test")
                .email("test@test.com")
                .image("test-image")
                .userRole(ROLE_MEMBER)
                .build();
        Member savedMember = memberRepository.save(member);
        Meeting meeting = Meeting.builder()
                .title("first meeting title")
                .content("first meeting content")
                .member(member)
                .imageUrl("imageUrl")
                .address("address")
                .coordinateX("coordinateX")
                .coordinateY("coordinateY")
                .placeName("placeName")
                .time(LocalDateTime.now().plusDays((1)))
                .recruitNum(5)
                .species("species")
                .build();
        meetingRepository.save(meeting);
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .apply(documentationConfiguration(restDocumentationContextProvider)
                        .operationPreprocessors()
                        .withRequestDefaults(modifyUris().host("tommy.me").removePort(), prettyPrint())
                        .withResponseDefaults(modifyUris().host("tommy.me").removePort(), prettyPrint()))
                .alwaysDo(print())
                .build();
    }

    @Test
    @DisplayName("정상적인 채팅방 생성")
    public void createChatRoom() throws Exception {
        ChatRoomRequestDto chatRoomRequestDto = new ChatRoomRequestDto("test room");
        Meeting meeting = meetingRepository.findByTitle("first meeting title").get();

        this.mockMvc.perform(post("/api/chat/" + meeting.getId())
                        .header("Authorization", getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(HAL_JSON)
                        .content(objectMapper.writeValueAsString(chatRoomRequestDto))
                )
                .andExpect(status().isCreated())
                .andDo(print())
                .andDo(document("create chatRoom",
                        requestHeaders(
                                headerWithName(HttpHeaders.ACCEPT).description("accept header"),
                                headerWithName(HttpHeaders.AUTHORIZATION).description("access token"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type")
                        ),
                        requestFields(
                                fieldWithPath("name").description("name of ChatRoom")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type")
                        ),
                        responseFields(
                                fieldWithPath("status").description("status of action"),
                                fieldWithPath("message").description("message of action"),
                                fieldWithPath("data.id").description("id of created chatRoom"),
                                fieldWithPath("data.meetingId").description("id of meetingRoom"),
                                fieldWithPath("data.roomName").description("name of chatRoom"),
                                fieldWithPath("data.links[0].rel").description("relation"),
                                fieldWithPath("data.links[0].href").description("url of action")
                        )
                ));
        Member member = memberRepository.findByEmail("test@test.com").get();
        ChatRoom chatRoom = chatRoomRepository.findByMeeting(meeting).get();
        ChatMember chatMember = chatMemberRepository.findByMember(member).get();
        Long memberId = member.getId();
        RedisChatRoom redisChatRoom = redisChatRoomRepository.findRoomById(String.valueOf(meeting.getId()));
        assertAll(
                () -> assertThat(chatRoomRepository.findAll().size()).isEqualTo(1),
                () -> assertThat(chatRoom.getRoomName()).isEqualTo("test room"),
                () -> assertThat(chatRoom.getMeeting().getMember().getId()).isEqualTo(memberId),
                () -> assertThat(chatMemberRepository.findAll().size()).isEqualTo(1),
                () -> assertThat(chatMember.getMember()).isEqualTo(member),
                () -> assertThat(chatMember.getChatRoom()).isEqualTo(chatRoom),
                () -> assertThat(chatMember.getMember()).isEqualTo(meeting.getMember()),
                () -> assertThat(chatRoom.getRoomId()).isEqualTo(redisChatRoom.getRoomId())
        );
    }

    @Test
    @DisplayName("Meeting 이 없을 경우 Error 처리")
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
        Member member = memberRepository.findByEmail("test@test.com").get();
        Meeting meeting = meetingRepository.findByTitle("first meeting title").get();
        Meeting meeting2 = Meeting.builder()
                .title("second meeting title")
                .content("first meeting content")
                .member(member)
                .imageUrl("imageUrl")
                .address("address")
                .coordinateX("coordinateX")
                .coordinateY("coordinateY")
                .placeName("placeName")
                .time(LocalDateTime.now().plusDays((1)))
                .recruitNum(5)
                .species("species")
                .build();
        meetingRepository.save(meeting);
        meetingRepository.save(meeting2);

        ChatRoom chatRoom1 = ChatRoom.builder()
                .meeting(meeting)
                .roomName("first chat room")
                .build();
        ChatRoom chatRoom2 = ChatRoom.builder()
                .meeting(meeting2)
                .roomName("second chat room")
                .build();
        chatRoomRepository.save(chatRoom1);
        chatRoomRepository.save(chatRoom2);

        this.mockMvc.perform(get("/api/chat")
                        .header("Authorization", getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(HAL_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("get chatRoomList",
                        requestHeaders(
                                headerWithName(HttpHeaders.ACCEPT).description("accept header"),
                                headerWithName(HttpHeaders.AUTHORIZATION).description("access token"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type")
                        ),
                        responseFields(
                                fieldWithPath("status").description("status of action"),
                                fieldWithPath("message").description("message of action"),
                                fieldWithPath("data.object[0].id").description("id of chatRoom"),
                                fieldWithPath("data.object[0].meetingId").description("id of meetingRoom"),
                                fieldWithPath("data.object[0].roomName").description("name of chatRoom"),
                                fieldWithPath("data.links[0].rel").description("relation"),
                                fieldWithPath("data.links[0].href").description("url of action")
                        )
                ));
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