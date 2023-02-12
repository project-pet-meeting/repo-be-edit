package sideproject.petmeeting.chat.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
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
import sideproject.petmeeting.chat.domain.ChatMessage;
import sideproject.petmeeting.chat.domain.ChatRoom;
import sideproject.petmeeting.chat.repository.ChatMemberRepository;
import sideproject.petmeeting.chat.repository.ChatMessageRepository;
import sideproject.petmeeting.chat.repository.ChatRoomRepository;
import sideproject.petmeeting.meeting.domain.Meeting;
import sideproject.petmeeting.meeting.repository.MeetingRepository;
import sideproject.petmeeting.member.domain.Member;
import sideproject.petmeeting.member.dto.request.LoginRequestDto;
import sideproject.petmeeting.member.repository.MemberRepository;

import java.time.LocalDateTime;

import static org.springframework.hateoas.MediaTypes.HAL_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.modifyUris;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static sideproject.petmeeting.chat.domain.MessageType.TALK;
import static sideproject.petmeeting.member.domain.UserRole.ROLE_MEMBER;

@Transactional
@ExtendWith({SpringExtension.class, RestDocumentationExtension.class})
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class ChatMessageControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    MeetingRepository meetingRepository;
    @Autowired
    ChatRoomRepository chatRoomRepository;
    @Autowired
    ChatMemberRepository chatMemberRepository;
    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @BeforeEach
    public void setup(WebApplicationContext webApplicationContext,
                      RestDocumentationContextProvider restDocumentationContextProvider) {

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
    @DisplayName("메세지가 있을 때 메세지 조회")
    void getMessageList() throws Exception {
        // Given
        Member member = buildMember();
        memberRepository.save(member);

        Meeting meeting = buildMeeting(member);
        meetingRepository.save(meeting);

        ChatRoom chatRoom = buildChatRoom(meeting);
        chatRoomRepository.save(chatRoom);

        ChatMember chatMember = buildChatMember(member, chatRoom);
        chatMemberRepository.save(chatMember);

        // When
        ChatMessage chatMessage = buildMessage(chatRoom, chatMember);
        chatMessageRepository.save(chatMessage);
        // Then
        this.mockMvc.perform(get("/api/message/" + chatRoom.getRoomId())
                        .header("Authorization", getAccessToken())
                        .contentType(APPLICATION_JSON)
                        .accept(HAL_JSON)
                )
                .andExpect(status().isOk())
                .andDo(document("get MessageList",
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
                                fieldWithPath("data.object[0].id").description("id of message"),
                                fieldWithPath("data.object[0].type").description("type of message"),
                                fieldWithPath("data.object[0].roomId").description("id of chatRoom"),
                                fieldWithPath("data.object[0].sender").description("message sender"),
                                fieldWithPath("data.object[0].senderImage").description("image of sender"),
                                fieldWithPath("data.object[0].message").description("content of message"),
                                fieldWithPath("data.links[0].rel").description("relation"),
                                fieldWithPath("data.links[0].href").description("url of action")
                        )
                ));

    }

    @Test
    @DisplayName("메세지가 없을 때 메세지 조회")
    void getMessageList_EmptyMessage() throws Exception {
        // Given
        Member member = buildMember();
        memberRepository.save(member);

        Meeting meeting = buildMeeting(member);
        meetingRepository.save(meeting);

        ChatRoom chatRoom = buildChatRoom(meeting);
        chatRoomRepository.save(chatRoom);

        ChatMember chatMember = buildChatMember(member, chatRoom);
        chatMemberRepository.save(chatMember);

        // When & Then
        this.mockMvc.perform(get("/api/message/" + chatRoom.getRoomId())
                        .header("Authorization", getAccessToken())
                        .contentType(APPLICATION_JSON)
                        .accept(HAL_JSON)
                )
                .andExpect(status().isOk());
    }

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

        return perform.andReturn().getResponse().getHeader("Authorization");
    }

    private static ChatMember buildChatMember(Member member, ChatRoom chatRoom) {
        ChatMember chatMember = ChatMember.builder()
                .chatRoom(chatRoom)
                .member(member)
                .build();
        return chatMember;
    }

    private static ChatRoom buildChatRoom(Meeting meeting) {
        ChatRoom chatRoom = ChatRoom.builder()
                .roomId("testRoomId")
                .meeting(meeting)
                .roomName("test ChatRoom")
                .build();
        return chatRoom;
    }

    private static Member buildMember() {
        Member member = Member.builder()
                .nickname("Tommy")
                .password("test")
                .email("test@test.com")
                .image("test-image")
                .userRole(ROLE_MEMBER)
                .build();
        return member;
    }

    private static Meeting buildMeeting(Member member) {
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
        return meeting;
    }

    private static ChatMessage buildMessage(ChatRoom chatRoom, ChatMember chatMember) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setMessage("test Message");
        chatMessage.setSender(String.valueOf(chatMember.getMember().getNickname()));
        chatMessage.setRoomId(chatRoom.getRoomId());
        chatMessage.setType(TALK);
        return chatMessage;
    }
}