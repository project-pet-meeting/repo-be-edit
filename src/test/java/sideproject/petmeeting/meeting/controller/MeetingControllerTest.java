package sideproject.petmeeting.meeting.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import sideproject.petmeeting.meeting.domain.Meeting;
import sideproject.petmeeting.meeting.dto.MeetingRequestDto;
import sideproject.petmeeting.meeting.repository.MeetingRepository;
import sideproject.petmeeting.member.domain.Member;
import sideproject.petmeeting.member.dto.request.LoginRequestDto;
import sideproject.petmeeting.member.repository.MemberRepository;

import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.hateoas.MediaTypes.HAL_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.modifyUris;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static sideproject.petmeeting.member.domain.UserRole.ROLE_MEMBER;

@ExtendWith({SpringExtension.class, RestDocumentationExtension.class})
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@TestMethodOrder(value = MethodOrderer.OrderAnnotation.class)
@Slf4j
class MeetingControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    MeetingRepository meetingRepository;
    public static final String USERNAME = "meetingController@Username.com";
    public static final String PASSWORD = "password";

    @BeforeEach
    public void setup(RestDocumentationContextProvider restDocumentationContextProvider) {

        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .apply(springSecurity())
                .apply(documentationConfiguration(restDocumentationContextProvider)
                        .operationPreprocessors()
                        .withRequestDefaults(modifyUris().host("tommy.me").removePort(), prettyPrint())
                        .withResponseDefaults(modifyUris().host("tommy.me").removePort(), prettyPrint()))
                .alwaysDo(print())
                .build();
    }

    @Order(0)
    @Test
    @DisplayName("공통으로 사용하는 ENTITY 생성")
    public void memberBuild() {
        Member member = Member.builder()
                .nickname(USERNAME)
                .password(PASSWORD)
                .email(USERNAME)
                .image("test-image")
                .userRole(ROLE_MEMBER)
                .build();
        memberRepository.save(member);
    }

    @Test
    @DisplayName("모임 생성 - 정상 응답")
    public void createMeeting() throws Exception {
        // Given
        MeetingRequestDto meetingRequestDto = MeetingRequestDto.builder()
                .title("first meeting title")
                .content("first meeting content")
                .address("address")
                .coordinateX("coordinateX")
                .coordinateY("coordinateY")
                .placeName("placeName")
                .time(LocalDateTime.parse("2022-12-25T18:00:00"))
                .recruitNum(5)
                .species("species")
                .build();

        String fileName = "jjang";
        String contentType = "png";
        String filePath = "src/test/resources/testImage/" + fileName + "." + contentType;
        FileInputStream fileInputStream = new FileInputStream(filePath);

        MockMultipartFile image = new MockMultipartFile(
                "image",
                fileName + "." + contentType,
                contentType,
                fileInputStream);

        String meetingRequestDtoJson = objectMapper.writeValueAsString(meetingRequestDto);
        MockMultipartFile data = new MockMultipartFile(
                "data",
                "data",
                "application/json",
                meetingRequestDtoJson.getBytes(StandardCharsets.UTF_8));

        // When
        mockMvc.perform(multipart("/api/meeting")
                        .file(data)
                        .file(image)
                        .header("Authorization", getAccessToken())
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(HAL_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("data.id").exists());

        // Then
        assertThat(meetingRequestDto.getTitle()).isEqualTo("first meeting title");
        assertThat(meetingRequestDto.getContent()).isEqualTo("first meeting content");
    }

    @Test
    @DisplayName("모임 생성 - data 값이 빈값으로 들어 온 경우 error 발생 (valid 유효성 검사)")
    public void createMeeting_DataEmpty() throws Exception {

        // Given
        MeetingRequestDto meetingRequestDto = MeetingRequestDto.builder()
                .build();

        // MockMultipartFile 을  MultipartFile 인터페이스를 상속받아 mock 구현
        String fileName = "jjang";
        String contentType = "png";
        String filePath = "src/test/resources/testImage/" + fileName + "." + contentType;
        FileInputStream fileInputStream = new FileInputStream(filePath);

        MockMultipartFile image = new MockMultipartFile(
                "image",
                fileName + "." + contentType,
                contentType,
                fileInputStream);

        String meetingRequestDtoJson = objectMapper.writeValueAsString(meetingRequestDto);
        MockMultipartFile data = new MockMultipartFile(
                "data",
                "data",
                "application/json",
                meetingRequestDtoJson.getBytes(StandardCharsets.UTF_8));

        // When & Then
        mockMvc.perform(multipart("/api/meeting")
                        .file(data)
                        .file(image)
                        .header("Authorization", getAccessToken()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("모임 전체 조회 - 정상 응답")
    public void getAllMeetings() throws Exception {
        // Given
        Member savedMember = memberRepository.findByNickname(USERNAME).orElseThrow();

        Meeting firstMeeting = Meeting.builder()
                .title("first meeting title")
                .content("first meeting content")
                .member(savedMember)
                .imageUrl("imageUrl")
                .address("address")
                .coordinateX("coordinateX")
                .coordinateY("coordinateY")
                .placeName("placeName")
                .time(LocalDateTime.parse("2022-12-25T18:00:00"))
                .recruitNum(5)
                .species("species")
                .build();
        meetingRepository.save(firstMeeting);

        Meeting secondMeeting = Meeting.builder()
                .title("second meeting title")
                .content("second meeting content")
                .member(savedMember)
                .imageUrl("imageUrl")
                .address("address")
                .coordinateX("coordinateX")
                .coordinateY("coordinateY")
                .placeName("placeName")
                .time(LocalDateTime.parse("2022-12-25T18:00:00"))
                .recruitNum(5)
                .species("species")
                .build();
        meetingRepository.save(secondMeeting);

        // When & Then
        this.mockMvc.perform(MockMvcRequestBuilders.get("/api/meeting")
                        .param("page", String.valueOf(0))
                        .header("Authorization", getAccessToken())
                        .contentType(APPLICATION_JSON)
                        .accept(HAL_JSON))
                .andExpect(status().isOk())
                .andDo(document("get-allMeetings",
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
                                        fieldWithPath("data.meetingList[].id").description("id of meeting"),
                                        fieldWithPath("data.meetingList[].title").description("title of meeting"),
                                        fieldWithPath("data.meetingList[].content").description("content of meeting"),
                                        fieldWithPath("data.meetingList[].imageUrl").description("imageUrl of meeting"),
                                        fieldWithPath("data.meetingList[].address").description("address of meeting"),
                                        fieldWithPath("data.meetingList[].coordinateX").description("coordinateX of meeting"),
                                        fieldWithPath("data.meetingList[].coordinateY").description("coordinateY of meeting"),
                                        fieldWithPath("data.meetingList[].placeName").description("placeName of meeting"),
                                        fieldWithPath("data.meetingList[].time").description("time of meeting"),
                                        fieldWithPath("data.meetingList[].recruitNum").description("recruitNum of meeting"),
                                        fieldWithPath("data.meetingList[].species").description("species of meeting"),
                                        fieldWithPath("data.meetingList[].authorId").description("authorId of meeting"),
                                        fieldWithPath("data.meetingList[].authorNickname").description("authorNickname of meeting"),
                                        fieldWithPath("data.meetingList[].authorImageUrl").description("authorImageUrl of meeting"),
                                        fieldWithPath("data.meetingList[].createdAt").description("createdAt of meeting"),
                                        fieldWithPath("data.meetingList[].modifiedAt").description("modifiedAt of meeting"),
                                        fieldWithPath("data.totalPage").description("totalPage of meetingList"),
                                        fieldWithPath("data.currentPage").description("currentPage of meetingList"),
                                        fieldWithPath("data.totalPost").description("totalPost of meetingList"),
                                        fieldWithPath("data.hasNextPage").description("hasNextPage of meetingList"),
                                        fieldWithPath("data.hasPreviousPage").description("hasPreviousPage of meetingList"),
                                        fieldWithPath("data.firstPage").description("firstPage of meetingList"),
                                        fieldWithPath("data.links[0].rel").description("relation"),
                                        fieldWithPath("data.links[0].href").description("url of action")
                                )
                        )
                )
        ;

    }

    @Test
    @DisplayName("모임 단건 조회 - 정상 응답")
    public void getMeeting() throws Exception {
        // Given
        Member savedMember = memberRepository.findByNickname(USERNAME).orElseThrow();

        Meeting firstMeeting = Meeting.builder()
                .title("first meeting title")
                .content("first meeting content")
                .member(savedMember)
                .imageUrl("imageUrl")
                .address("address")
                .coordinateX("coordinateX")
                .coordinateY("coordinateY")
                .placeName("placeName")
                .time(LocalDateTime.parse("2022-12-25T18:00:00"))
                .recruitNum(5)
                .species("species")
                .build();
        meetingRepository.save(firstMeeting);

        // When
        this.mockMvc.perform(MockMvcRequestBuilders.get("/api/meeting/" + firstMeeting.getId())
                        .header("Authorization", getAccessToken())
                        .contentType(APPLICATION_JSON)
                        .accept(HAL_JSON))
                .andExpect(status().isOk())
                .andDo(document("get-Meeting",
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
                                        fieldWithPath("data.id").description("id of meeting"),
                                        fieldWithPath("data.title").description("title of meeting"),
                                        fieldWithPath("data.content").description("content of meeting"),
                                        fieldWithPath("data.imageUrl").description("imageUrl of meeting"),
                                        fieldWithPath("data.address").description("address of meeting"),
                                        fieldWithPath("data.coordinateX").description("coordinateX of meeting"),
                                        fieldWithPath("data.coordinateY").description("coordinateY of meeting"),
                                        fieldWithPath("data.placeName").description("placeName of meeting"),
                                        fieldWithPath("data.time").description("time of meeting"),
                                        fieldWithPath("data.recruitNum").description("recruitNum of meeting"),
                                        fieldWithPath("data.species").description("species of meeting"),
                                        fieldWithPath("data.authorId").description("authorId of meeting"),
                                        fieldWithPath("data.authorNickname").description("authorNickname of meeting"),
                                        fieldWithPath("data.authorImageUrl").description("authorImageUrl of meeting"),
                                        fieldWithPath("data.createdAt").description("createdAt of meeting"),
                                        fieldWithPath("data.modifiedAt").description("modifiedAt of meeting"),
                                        fieldWithPath("data.links[0].rel").description("relation"),
                                        fieldWithPath("data.links[0].href").description("url of action")
                                )
                        )
                )
        ;

        // Then
        assertThat(firstMeeting.getTitle()).isEqualTo("first meeting title");
        assertThat(firstMeeting.getContent()).isEqualTo("first meeting content");
        assertThat(firstMeeting.getMember().getNickname()).isEqualTo(USERNAME);
    }

    @Test
    @DisplayName("모임 단건 조회 - 모임이 존재하지 않는 경우 Error")
    public void getMeeting_No_Meeting() throws Exception {
        // Given
        Member savedMember = memberRepository.findByNickname(USERNAME).orElseThrow();

        Meeting firstMeeting = Meeting.builder()
                .title("first meeting title")
                .content("first meeting content")
                .member(savedMember)
                .imageUrl("imageUrl")
                .address("address")
                .coordinateX("coordinateX")
                .coordinateY("coordinateY")
                .placeName("placeName")
                .time(LocalDateTime.parse("2022-12-25T18:00:00"))
                .recruitNum(5)
                .species("species")
                .build();
        meetingRepository.save(firstMeeting);

        // When
        this.mockMvc.perform(MockMvcRequestBuilders.get("/api/meeting/" + firstMeeting.getId() + 1)
                        .header("Authorization", getAccessToken())
                        .contentType(APPLICATION_JSON)
                        .accept(HAL_JSON))
                .andExpect(status().is4xxClientError())
        ;
    }

    @Test
    @DisplayName("모임 수정 - 정상 응답")
    public void updateMeeting() throws Exception {
        // Given
        Member savedMember = memberRepository.findByNickname(USERNAME).orElseThrow();

        Meeting firstMeeting = Meeting.builder()
                .title("first meeting title")
                .content("first meeting content")
                .member(savedMember)
                .imageUrl("imageUrl")
                .address("address")
                .coordinateX("coordinateX")
                .coordinateY("coordinateY")
                .placeName("placeName")
                .time(LocalDateTime.parse("2022-12-25T18:00:00"))
                .recruitNum(5)
                .species("species")
                .build();
        meetingRepository.save(firstMeeting);

        MeetingRequestDto meetingRequestDto = MeetingRequestDto.builder()
                .title("수정 제목")
                .content("수정 내용")
                .address("address")
                .coordinateX("coordinateX")
                .coordinateY("coordinateY")
                .placeName("placeName")
                .time(LocalDateTime.parse("2022-12-25T18:00:00"))
                .recruitNum(5)
                .species("species")
                .build();

        String fileName = "memberImage";
        String contentType = "jpeg";
        String filePath = "src/test/resources/testImage/" + fileName + "." + contentType;
        FileInputStream fileInputStream = new FileInputStream(filePath);

        MockMultipartFile image = new MockMultipartFile(
                "image",
                fileName + "." + contentType,
                contentType,
                fileInputStream);

        String meetingRequestDtoJson = objectMapper.writeValueAsString(meetingRequestDto);
        MockMultipartFile data = new MockMultipartFile(
                "data",
                "data",
                "application/json",
                meetingRequestDtoJson.getBytes(StandardCharsets.UTF_8));

        // When & Then
        mockMvc.perform(multipartPutBuilder("/api/meeting/" + firstMeeting.getId())
                        .file(data)
                        .file(image)
                        .header("Authorization", getAccessToken())
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(HAL_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("data.id").exists());

        // Then
        assertThat(meetingRequestDto.getTitle()).isEqualTo("수정 제목");
        assertThat(meetingRequestDto.getContent()).isEqualTo("수정 내용");
    }

    @Test
    @DisplayName("모임 수정 - 권한이 없는 경우 Error")
    public void updateMeeting_Not_Authorization() throws Exception {
        // Given
        Member member2 = Member.builder()
                .nickname("meetingModify")
                .password(PASSWORD)
                .email("meetingModify")
                .image("test-image")
                .userRole(ROLE_MEMBER)
                .build();
        memberRepository.save(member2);

        Meeting firstMeeting = Meeting.builder()
                .title("first meeting title")
                .content("first meeting content")
                .member(memberRepository.findByNickname("meetingModify").orElseThrow())
                .imageUrl("imageUrl")
                .address("address")
                .coordinateX("coordinateX")
                .coordinateY("coordinateY")
                .placeName("placeName")
                .time(LocalDateTime.parse("2022-12-25T18:00:00"))
                .recruitNum(5)
                .species("species")
                .build();
        meetingRepository.save(firstMeeting);

        MeetingRequestDto meetingRequestDto = MeetingRequestDto.builder()
                .title("수정 제목")
                .content("수정 내용")
                .address("address")
                .coordinateX("coordinateX")
                .coordinateY("coordinateY")
                .placeName("placeName")
                .time(LocalDateTime.parse("2022-12-25T18:00:00"))
                .recruitNum(5)
                .species("species")
                .build();

        String fileName = "memberImage";
        String contentType = "jpeg";
        String filePath = "src/test/resources/testImage/" + fileName + "." + contentType;
        FileInputStream fileInputStream = new FileInputStream(filePath);

        MockMultipartFile image = new MockMultipartFile(
                "image",
                fileName + "." + contentType,
                contentType,
                fileInputStream);

        String meetingRequestDtoJson = objectMapper.writeValueAsString(meetingRequestDto);
        MockMultipartFile data = new MockMultipartFile(
                "data",
                "data",
                "application/json",
                meetingRequestDtoJson.getBytes(StandardCharsets.UTF_8));

        // When & Then
        mockMvc.perform(multipartPutBuilder("/api/meeting/" + firstMeeting.getId())
                        .file(data)
                        .file(image)
                        .header("Authorization", getAccessToken())
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(HAL_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("모임 삭제 - 정상 응답")
    public void deleteMeeting() throws Exception {
        // Given
        Member savedMember = memberRepository.findByNickname(USERNAME).orElseThrow();

        Meeting firstMeeting = Meeting.builder()
                .title("first meeting title")
                .content("first meeting content")
                .member(savedMember)
                .imageUrl("imageUrl")
                .address("address")
                .coordinateX("coordinateX")
                .coordinateY("coordinateY")
                .placeName("placeName")
                .time(LocalDateTime.parse("2022-12-25T18:00:00"))
                .recruitNum(5)
                .species("species")
                .build();
        meetingRepository.save(firstMeeting);

        // When
        this.mockMvc.perform(MockMvcRequestBuilders.delete("/api/meeting/" + firstMeeting.getId())
                        .param("meetingId", firstMeeting.getId().toString())
                        .header("Authorization", getAccessToken())
                        .contentType(APPLICATION_JSON)
                        .accept(HAL_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("delete-meeting",
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
                                        fieldWithPath("data.links[0].rel").description("relation"),
                                        fieldWithPath("data.links[0].href").description("url of action")
                                )
                        )
                )
        ;

    }

    @Test
    @DisplayName("모임 삭제 - 권한이 없는 경우 Error")
    public void deleteMeeting_Not_Authorization() throws Exception {
        // Given
        Member member2 = Member.builder()
                .nickname("meetingDelete")
                .password(PASSWORD)
                .email("meetingDelete")
                .image("test-image")
                .userRole(ROLE_MEMBER)
                .build();
        memberRepository.save(member2);

        Meeting firstMeeting = Meeting.builder()
                .title("first meeting title")
                .content("first meeting content")
                .member(memberRepository.findByNickname("meetingDelete").orElseThrow())
                .imageUrl("imageUrl")
                .address("address")
                .coordinateX("coordinateX")
                .coordinateY("coordinateY")
                .placeName("placeName")
                .time(LocalDateTime.parse("2022-12-25T18:00:00"))
                .recruitNum(5)
                .species("species")
                .build();
        meetingRepository.save(firstMeeting);

        // When
        this.mockMvc.perform(MockMvcRequestBuilders.delete("/api/meeting/" + firstMeeting.getId())
                        .param("meetingId", firstMeeting.getId().toString())
                        .header("Authorization", getAccessToken())
                        .contentType(APPLICATION_JSON)
                        .accept(HAL_JSON))
                .andExpect(status().is4xxClientError())
                .andDo(print());
    }

    private String getAccessToken() throws Exception {
        // Given
        LoginRequestDto loginRequestDto = new LoginRequestDto(USERNAME, PASSWORD);

        // When & Then
        ResultActions perform = this.mockMvc.perform(post("/api/member/login")
                        .contentType(APPLICATION_JSON)
                        .accept(HAL_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDto)))
                .andDo(print())
                .andExpect(status().isOk());
//        assertThat(refreshTokenRepository.findAll().size()).isEqualTo(1);

        return perform.andReturn().getResponse().getHeader("Authorization");
    }

    private MockMultipartHttpServletRequestBuilder multipartPutBuilder(final String url) {
        final MockMultipartHttpServletRequestBuilder builder = MockMvcRequestBuilders.multipart(url);
        builder.with(request1 -> {
            request1.setMethod(HttpMethod.PUT.name());
            return request1;
        });
        return builder;
    }
}