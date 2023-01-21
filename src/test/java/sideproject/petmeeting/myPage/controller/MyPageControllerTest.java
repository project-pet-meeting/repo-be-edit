package sideproject.petmeeting.myPage.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import sideproject.petmeeting.meeting.domain.Meeting;
import sideproject.petmeeting.meeting.repository.AttendanceRepository;
import sideproject.petmeeting.meeting.repository.MeetingRepository;
import sideproject.petmeeting.member.domain.Member;
import sideproject.petmeeting.member.dto.request.LoginRequestDto;
import sideproject.petmeeting.member.repository.MemberRepository;
import sideproject.petmeeting.pet.domain.Pet;
import sideproject.petmeeting.pet.repository.PetRepository;
import sideproject.petmeeting.post.domain.HeartPost;
import sideproject.petmeeting.post.domain.Post;
import sideproject.petmeeting.post.repository.HeartPostRepository;
import sideproject.petmeeting.post.repository.PostRepository;
import sideproject.petmeeting.token.repository.RefreshTokenRepository;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static sideproject.petmeeting.member.domain.UserRole.ROLE_MEMBER;
import static sideproject.petmeeting.post.domain.Category.*;

@ExtendWith({SpringExtension.class, RestDocumentationExtension.class})
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@TestMethodOrder(value = MethodOrderer.OrderAnnotation.class)
@Slf4j
class MyPageControllerTest {

    @Autowired
    private HeartPostRepository heartPostRepository;
    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private MeetingRepository meetingRepository;
    @Autowired
    private PetRepository petRepository;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    @Autowired
    private AttendanceRepository attendanceRepository;

    public static final String USERNAME = "mypageController@Username.com";
    public static final String PASSWORD = "password";

    @BeforeEach
    public void setup(RestDocumentationContextProvider restDocumentationContextProvider) {

        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .apply(springSecurity())
                .apply(documentationConfiguration(restDocumentationContextProvider)
                        .operationPreprocessors()
                        .withRequestDefaults(modifyUris().host("localhost").removePort(), prettyPrint())
                        .withResponseDefaults(modifyUris().host("localhost").removePort(), prettyPrint()))
                .alwaysDo(print())
                .build();

        Member member = Member.builder()
                .nickname(USERNAME)
                .password(PASSWORD)
                .email(USERNAME)
                .location("서울")
                .image("test-image")
                .userRole(ROLE_MEMBER)
                .build();
        memberRepository.save(member);
    }

    @AfterEach
    public void after() {
        petRepository.deleteAllInBatch();
        attendanceRepository.deleteAllInBatch();
        meetingRepository.deleteAllInBatch();
        heartPostRepository.deleteAllInBatch();
        postRepository.deleteAllInBatch();
        refreshTokenRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }

//    @Order(0)
//    @Test
//    @DisplayName("공통으로 사용하는 ENTITY 생성")
//    public void entityBuild() {
//        Member member = Member.builder()
//                .nickname(USERNAME)
//                .password(PASSWORD)
//                .email(USERNAME)
//                .location("서울")
//                .image("test-image")
//                .userRole(ROLE_MEMBER)
//                .build();
//        memberRepository.save(member);
//    }

    @Test
    @Transactional
    @DisplayName("내 정보 조회 - 정상응답")
    public void getMyProfile() throws Exception {
        log.info("내 정보 조회 시작");

        // When
        this.mockMvc.perform(MockMvcRequestBuilders.get("/api/mypage")
                        .header("Authorization", getAccessToken())
                        .contentType(APPLICATION_JSON)
                        .accept(HAL_JSON))
                .andExpect(status().isOk())
                .andDo(document("{class-name}/{method-name}",
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
                                        fieldWithPath("data.id").description("id of member"),
                                        fieldWithPath("data.nickname").description("nickname of member"),
                                        fieldWithPath("data.email").description("email of member"),
                                        fieldWithPath("data.location").description("location of member"),
                                        fieldWithPath("data.image").description("image of member"),
                                        fieldWithPath("data.pet[]").description("image of member"),
                                        fieldWithPath("data.links[0].rel").description("relation"),
                                        fieldWithPath("data.links[0].href").description("url of action")
                                )
                        )
                )
        ;

        // Then
        Member assertMember = memberRepository.findByNickname(USERNAME).orElseThrow();

        assertThat(assertMember.getNickname()).isEqualTo(USERNAME);
        assertThat(assertMember.getEmail()).isEqualTo(USERNAME);
        assertThat(assertMember.getImage()).isEqualTo("test-image");
        log.info("내 정보 조회 종료");

    }

    @Test
    @Transactional
    @DisplayName("타유저 정보 조회 - 정상응답")
    public void getMemberProfile() throws Exception {
        log.info("타유저 정보 조회 시작");
        // Given
        Member member2 = Member.builder()
                .nickname("memberProfile")
                .password(PASSWORD)
                .email("memberProfile")
                .location("서울")
                .image("test-image")
                .userRole(ROLE_MEMBER)
                .build();
        memberRepository.save(member2);

        Pet pet = Pet.builder()
                .name("멍멍이")
                .age(2)
                .weight(2.5)
                .species("강아지")
                .gender("남")
                .imageUrl("test-image")
                .member(member2)
                .build();
        petRepository.save(pet);

        // When
        this.mockMvc.perform(MockMvcRequestBuilders.get("/api/mypage/" + member2.getId())
                        .header("Authorization", getAccessToken())
                        .contentType(APPLICATION_JSON)
                        .accept(HAL_JSON))
                .andExpect(status().isOk())
                .andDo(document("{class-name}/{method-name}",
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
                                        fieldWithPath("data.id").description("id of member"),
                                        fieldWithPath("data.nickname").description("nickname of member"),
                                        fieldWithPath("data.location").description("location of member"),
                                        fieldWithPath("data.image").description("image of member"),
                                        fieldWithPath("data.pet[]").description("id of pet"),
                                        fieldWithPath("data.pet[].id").description("id of pet"),
                                        fieldWithPath("data.pet[].name").description("name of pet"),
                                        fieldWithPath("data.pet[].age").description("age of pet"),
                                        fieldWithPath("data.pet[].weight").description("weight of pet"),
                                        fieldWithPath("data.pet[].species").description("species of pet"),
                                        fieldWithPath("data.pet[].gender").description("gender of pet"),
                                        fieldWithPath("data.pet[].imageUrl").description("imageUrl of pet"),
                                        fieldWithPath("data.pet[].createdAt").description("createdAt of pet"),
                                        fieldWithPath("data.pet[].modifiedAt").description("modifiedAt of pet"),
                                        fieldWithPath("data.links[0].rel").description("relation"),
                                        fieldWithPath("data.links[0].href").description("url of action")
                                )
                        )
                )
        ;

        // Then
        Member assertMember = memberRepository.findById(member2.getId()).orElseThrow();
        Pet assertPet = petRepository.findById(pet.getId()).orElseThrow();

        assertThat(assertMember.getNickname()).isEqualTo("memberProfile");
        assertThat(assertMember.getEmail()).isEqualTo("memberProfile");
        assertThat(assertMember.getImage()).isEqualTo("test-image");
        assertThat(assertPet.getName()).isEqualTo("멍멍이");
        assertThat(assertPet.getSpecies()).isEqualTo("강아지");

        log.info("타유저 정보 조회 종료");

    }

    @Test
    @Transactional
    @DisplayName("내가 작성한 게시글 조회 - 정상응답")
    public void getMyPosts() throws Exception {
        log.info("내가 작성한 게시글 조회 시작");

        // Given
        Member savedMember = memberRepository.findByNickname(USERNAME).orElseThrow();

        Post firstPost = Post.builder()
                .category(RECOMMEND)
                .title("first post title")
                .content("first post content")
                .member(savedMember)
                .imageUrl("imageUrl")
                .numHeart(0)
                .build();
        postRepository.save(firstPost);

        Post secondPost = Post.builder()
                .category(SHARE)
                .title("second post title")
                .content("second post content")
                .member(savedMember)
                .imageUrl("imageUrl")
                .numHeart(0)
                .viewCnt(0)
                .build();
        postRepository.save(secondPost);

        // When & Then
        this.mockMvc.perform(MockMvcRequestBuilders.get("/api/mypage/post")
                        .param("page", String.valueOf(0))
                        .header("Authorization", getAccessToken())
                        .contentType(APPLICATION_JSON)
                        .accept(HAL_JSON))
                .andExpect(status().isOk())
                .andDo(document("{class-name}/{method-name}",
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
                                        fieldWithPath("data.myPostList[].id").description("id of post"),
                                        fieldWithPath("data.myPostList[].category").description("category of post"),
                                        fieldWithPath("data.myPostList[].title").description("title of post"),
                                        fieldWithPath("data.myPostList[].content").description("content of post"),
                                        fieldWithPath("data.myPostList[].imageUrl").description("imageUrl of post"),
                                        fieldWithPath("data.myPostList[].numHeart").description("numHeart of post"),
                                        fieldWithPath("data.myPostList[].viewCnt").description("viewCnt of post"),
                                        fieldWithPath("data.myPostList[].authorId").description("authorId of post"),
                                        fieldWithPath("data.myPostList[].authorNickname").description("authorNickname of post"),
                                        fieldWithPath("data.myPostList[].authorLocation").description("authorLocation of post"),
                                        fieldWithPath("data.myPostList[].authorImageUrl").description("authorImageUrl of post"),
                                        fieldWithPath("data.myPostList[].createdAt").description("createdAt of post"),
                                        fieldWithPath("data.myPostList[].modifiedAt").description("modifiedAt of post"),
                                        fieldWithPath("data.links[0].rel").description("relation"),
                                        fieldWithPath("data.links[0].href").description("url of action")
                                )
                        )
                )
        ;
        log.info("내가 작성한 게시글 조회 종료");
    }

    @Test
    @Transactional
    @DisplayName("내가 생성한 모임 조회 - 정상응답")
    public void getMyMeetings() throws Exception {
        log.info("내가 생성한 모임 조회 시작");

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
                .time(LocalDateTime.now().plusDays((1)))
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
                .time(LocalDateTime.now().plusDays((1)))
                .recruitNum(5)
                .species("species")
                .build();
        meetingRepository.save(secondMeeting);

        // When & Then
        this.mockMvc.perform(MockMvcRequestBuilders.get("/api/mypage/meeting")
                        .param("page", String.valueOf(0))
                        .header("Authorization", getAccessToken())
                        .contentType(APPLICATION_JSON)
                        .accept(HAL_JSON))
                .andExpect(status().isOk())
                .andDo(document("{class-name}/{method-name}",
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
                                        fieldWithPath("data.myMeetingList[].id").description("id of meeting"),
                                        fieldWithPath("data.myMeetingList[].title").description("title of meeting"),
                                        fieldWithPath("data.myMeetingList[].content").description("content of meeting"),
                                        fieldWithPath("data.myMeetingList[].imageUrl").description("imageUrl of meeting"),
                                        fieldWithPath("data.myMeetingList[].address").description("address of meeting"),
                                        fieldWithPath("data.myMeetingList[].coordinateX").description("coordinateX of meeting"),
                                        fieldWithPath("data.myMeetingList[].coordinateY").description("coordinateY of meeting"),
                                        fieldWithPath("data.myMeetingList[].placeName").description("placeName of meeting"),
                                        fieldWithPath("data.myMeetingList[].time").description("time of meeting"),
                                        fieldWithPath("data.myMeetingList[].recruitNum").description("recruitNum of meeting"),
                                        fieldWithPath("data.myMeetingList[].currentNum").description("currentNum of meeting"),
                                        fieldWithPath("data.myMeetingList[].species").description("species of meeting"),
                                        fieldWithPath("data.myMeetingList[].authorId").description("authorId of meeting"),
                                        fieldWithPath("data.myMeetingList[].authorNickname").description("authorNickname of meeting"),
                                        fieldWithPath("data.myMeetingList[].authorImageUrl").description("authorImageUrl of meeting"),
                                        fieldWithPath("data.myMeetingList[].createdAt").description("createdAt of meeting"),
                                        fieldWithPath("data.myMeetingList[].modifiedAt").description("modifiedAt of meeting"),
                                        fieldWithPath("data.links[0].rel").description("relation"),
                                        fieldWithPath("data.links[0].href").description("url of action")
                                )
                        )
                )
        ;
        log.info("내가 생성한 모임 조회 종료");


    }

    @Test
    @Transactional
    @DisplayName("내가 좋아요한 게시글 조회 - 정상응답")
    public void getMyHeartPosts() throws Exception {
        log.info("내가 좋아요한 게시글 조회 시작");

        // Given
        Member savedMember = memberRepository.findByNickname(USERNAME).orElseThrow();


        log.info("첫번째 게시글 작성");
        Post firstPost = Post.builder()
                .category(RECOMMEND)
                .title("first post title")
                .content("first post content")
                .member(savedMember)
                .imageUrl("imageUrl")
                .numHeart(0)
                .viewCnt(0)
                .build();
        postRepository.save(firstPost);

        log.info("두번째 게시글 작성");
        Post secondPost = Post.builder()
                .category(SHARE)
                .title("second post title")
                .content("second post content")
                .member(savedMember)
                .imageUrl("imageUrl")
                .numHeart(0)
                .viewCnt(0)
                .build();
        postRepository.save(secondPost);

        log.info("첫번째 게시글에 좋아요");
        HeartPost heartPost = HeartPost.builder()
                .post(postRepository.findById(firstPost.getId()).orElseThrow())
                .member(savedMember)
                .build();
        heartPostRepository.save(heartPost);

        // When & Then
        this.mockMvc.perform(MockMvcRequestBuilders.get("/api/mypage/heart")
                        .param("page", String.valueOf(0))
                        .header("Authorization", getAccessToken())
                        .contentType(APPLICATION_JSON)
                        .accept(HAL_JSON))
                .andExpect(status().isOk())
                .andDo(document("{class-name}/{method-name}",
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
                                        fieldWithPath("data.myHeartPostList[].id").description("id of post"),
                                        fieldWithPath("data.myHeartPostList[].category").description("category of post"),
                                        fieldWithPath("data.myHeartPostList[].title").description("title of post"),
                                        fieldWithPath("data.myHeartPostList[].content").description("content of post"),
                                        fieldWithPath("data.myHeartPostList[].imageUrl").description("imageUrl of post"),
                                        fieldWithPath("data.myHeartPostList[].numHeart").description("numHeart of post"),
                                        fieldWithPath("data.myHeartPostList[].viewCnt").description("viewCnt of post"),
                                        fieldWithPath("data.myHeartPostList[].authorId").description("authorId of post"),
                                        fieldWithPath("data.myHeartPostList[].authorNickname").description("authorNickname of post"),
                                        fieldWithPath("data.myHeartPostList[].authorImageUrl").description("authorImageUrl of post"),
                                        fieldWithPath("data.myHeartPostList[].authorLocation").description("authorLocation of post"),
                                        fieldWithPath("data.myHeartPostList[].createdAt").description("createdAt of post"),
                                        fieldWithPath("data.myHeartPostList[].modifiedAt").description("modifiedAt of post"),
                                        fieldWithPath("data.links[0].rel").description("relation"),
                                        fieldWithPath("data.links[0].href").description("url of action")
                                )
                        )
                )
        ;
        log.info("내가 좋아요한 게시글 조회 종료");

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

        return perform.andReturn().getResponse().getHeader("Authorization")

                ;
    }






}