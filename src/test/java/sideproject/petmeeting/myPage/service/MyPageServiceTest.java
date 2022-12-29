package sideproject.petmeeting.myPage.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.filter.CharacterEncodingFilter;
import sideproject.petmeeting.meeting.domain.Meeting;
import sideproject.petmeeting.meeting.repository.MeetingRepository;
import sideproject.petmeeting.member.domain.Member;
import sideproject.petmeeting.member.repository.MemberRepository;
import sideproject.petmeeting.myPage.dto.MyHeartPostDto;
import sideproject.petmeeting.myPage.dto.MyMeetingDto;
import sideproject.petmeeting.myPage.dto.MyPostDto;
import sideproject.petmeeting.myPage.dto.ProfileDto;
import sideproject.petmeeting.pet.domain.Pet;
import sideproject.petmeeting.pet.repository.PetRepository;
import sideproject.petmeeting.post.domain.HeartPost;
import sideproject.petmeeting.post.domain.Post;
import sideproject.petmeeting.post.repository.HeartPostRepository;
import sideproject.petmeeting.post.repository.PostRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.modifyUris;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static sideproject.petmeeting.member.domain.UserRole.ROLE_MEMBER;
import static sideproject.petmeeting.post.domain.Category.RECOMMEND;

@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(value = MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
@Slf4j
class MyPageServiceTest {
    @Autowired
    MyPageService myPageService;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private MeetingRepository meetingRepository;
    @Autowired
    private HeartPostRepository heartPostRepository;
    @Autowired
    private PetRepository petRepository;

    public static final String USERNAME = "mypageService@Username.com";
    public static final String PASSWORD = "password";

    @BeforeEach
    public void setup() {
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
        meetingRepository.deleteAllInBatch();
        heartPostRepository.deleteAllInBatch();
        postRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }


    @Test
    @Order(1)
    @DisplayName("공통으로 사용하는 ENTITY 생성")
    public void entityBuild() {
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

    @Test
    @Transactional
    @DisplayName("내 정보 조회 - 정상응답")
    public void getMyProfileTest() {
        log.info("내 정보 조회 정상 응답 테스트 시작");
        // Given
        log.info("저장한 회원 id로 조회하여 member 가져오기");
        Member savedMember = memberRepository.findByNickname(USERNAME).orElseThrow();

        // When
        log.info("내 정보 조회 서비스 로직");
        ProfileDto profileDto = myPageService.getProfile(savedMember);

        // Then
        assertThat(profileDto.getNickname()).isEqualTo(USERNAME);
        assertThat(profileDto.getEmail()).isEqualTo(USERNAME);
        assertThat(profileDto.getLocation()).isEqualTo("서울");
        assertThat(profileDto.getImage()).isEqualTo("test-image");
        log.info("내 정보 조회 정상 응답 테스트 종료");
    }

    @Test
    @Transactional
    @DisplayName("타유저 정보 조회 - 정상응답")
    public void getMemberProfileTest() {
        log.info("타유저 정보 조회 정상 응답 테스트 시작");
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
        log.info("타유저 정보 조회 서비스 로직");
        ProfileDto profileDto = myPageService.getMemberProfile(member2.getId());

        // Then
        assertThat(profileDto.getNickname()).isEqualTo("memberProfile");
        assertThat(profileDto.getLocation()).isEqualTo("서울");
        assertThat(profileDto.getImage()).isEqualTo("test-image");
        log.info("타유저 정보 조회 정상 응답 테스트 종료");
    }

    @Test
    @DisplayName("내가 작성한 게시글 조회 테스트 - 정상응답")
    public void getMyPostsTest() {
        log.info("내가 작성한 게시글 조회 정상 응답 테스트 시작");
        // Given
        log.info("저장한 회원 id로 조회하여 member 가져오기");
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
                .category(RECOMMEND)
                .title("second post title")
                .content("second post content")
                .member(savedMember)
                .imageUrl("imageUrl")
                .numHeart(0)
                .build();
        postRepository.save(secondPost);

        // When
        MyPostDto myPostDto = myPageService.getMyPost(savedMember);

        // Then
//        assertThat(myPostDto.getMyPostList().stream().count()).isEqualTo(2);
        log.info("내가 작성한 게시글 조회 정상 응답 테스트 종료");
    }

    @Test
    @Transactional
    @DisplayName("내가 생성한 모임 조회 테스트")
    public void getMyMeetingsTest() {
        log.info("내가 생성한 모임 조회 정상 응답 테스트 시작");
        // Given
        log.info("저장한 회원 id로 조회하여 member 가져오기");
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

        // When
        MyMeetingDto myMeetingDto = myPageService.getMyMeeting(savedMember);

        // Then
        assertThat((long) myMeetingDto.getMyMeetingList().size()).isEqualTo(2);
        log.info("내가 생성한 모임 조회 정상 응답 테스트 종료");
    }

    @Test
    @Transactional
    @DisplayName("내가 좋아요한 게시글 조회 테스트")
    public void getMyHeartPostsTest() {
        log.info("내가 좋아요한 게시글 조회 정상 응답 테스트 시작");
        // Given
        log.info("저장한 회원 id로 조회하여 member 가져오기");
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
                .category(RECOMMEND)
                .title("second post title")
                .content("second post content")
                .member(savedMember)
                .imageUrl("imageUrl")
                .numHeart(0)
                .build();
        postRepository.save(secondPost);

        HeartPost heartPost = HeartPost.builder()
                .post(firstPost)
                .member(savedMember)
                .build();
        heartPostRepository.save(heartPost);

        // When
        MyHeartPostDto myHeartPostDto = myPageService.getMyHeartPost(savedMember);

        // Then
//        assertThat((long) myHeartPostDto.getMyHeartPostList().size()).isEqualTo(1);
        log.info("내가 좋아요한 게시글 조회 정상 응답 테스트 종료");

    }

}