package sideproject.petmeeting.meeting.service;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import sideproject.petmeeting.meeting.domain.Meeting;
import sideproject.petmeeting.meeting.dto.MeetingPageResponseDto;
import sideproject.petmeeting.meeting.dto.MeetingRequestDto;
import sideproject.petmeeting.meeting.dto.MeetingResponseDto;
import sideproject.petmeeting.meeting.repository.AttendanceRepository;
import sideproject.petmeeting.meeting.repository.MeetingRepository;
import sideproject.petmeeting.member.domain.Member;
import sideproject.petmeeting.member.repository.MemberRepository;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static sideproject.petmeeting.member.domain.UserRole.ROLE_MEMBER;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@TestMethodOrder(value = MethodOrderer.OrderAnnotation.class)
class MeetingServiceTest {

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private MeetingRepository meetingRepository;
    @Autowired
    private AttendanceRepository attendanceRepository;
    @Autowired
    MeetingService meetingService;
    public static final String USERNAME = "meetingService@Username.com";
    public static final String PASSWORD = "password";


    @BeforeEach
    public void setup() {
        Member member = Member.builder()
                .nickname(USERNAME)
                .password(PASSWORD)
                .email(USERNAME)
                .image("test-image")
                .userRole(ROLE_MEMBER)
                .build();
        memberRepository.save(member);
    }

    @AfterEach
    public void after() {
        attendanceRepository.deleteAllInBatch();
        meetingRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }

//    @Test
//    @Order(0)
//    @DisplayName("공통으로 사용하는 ENTITY 생성")
//    public void entityBuild() {
//        Member member = Member.builder()
//                .nickname(USERNAME)
//                .password(PASSWORD)
//                .email(USERNAME)
//                .image("test-image")
//                .userRole(ROLE_MEMBER)
//                .build();
//        memberRepository.save(member);
//    }

    @Test
    @Transactional
    @DisplayName("모임 생성 테스트 - 정상 응답")
    public void createMeeting() throws IOException {
        // Given
        MeetingRequestDto meetingRequestDto = MeetingRequestDto.builder()
                .title("first meeting title")
                .content("first meeting content")
                .address("address")
                .coordinateX("coordinateX")
                .coordinateY("coordinateY")
                .placeName("placeName")
                .time(LocalDateTime.now().plusDays((1)))
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

        Member savedMember = memberRepository.findByNickname(USERNAME).orElseThrow();
        // When
        MeetingResponseDto savedMeeting = meetingService.createMeeting(meetingRequestDto, image, savedMember);

        // Then
        assertThat(savedMeeting.getTitle()).isEqualTo("first meeting title");
        assertThat(savedMeeting.getContent()).isEqualTo("first meeting content");
    }

    @Test
    @Transactional
    @DisplayName("모임 전체 조회 테스트 - 정상 응답")
    public void getAllMeeting() {
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

        // When
        MeetingPageResponseDto savedMeeting = meetingService.getAllMeeting(0);

        // Then
//        assertThat(savedMeeting).isEqualTo(2);

    }

    @Test
    @Transactional
    @DisplayName("모임 단일 조회 테스트 - 정상 응답")
    public void getMeeting() {
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

        // When
        MeetingResponseDto savedMeeting = meetingService.getMeeting(firstMeeting.getId());

        // Then
        assertThat(savedMeeting.getTitle()).isEqualTo("first meeting title");
        assertThat(savedMeeting.getContent()).isEqualTo("first meeting content");
    }

    @Test
    @Transactional
    @DisplayName("모임 수정 테스트 - 정상 응답")
    public void updateMeeting() throws IOException {
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

        MeetingRequestDto meetingRequestDto = MeetingRequestDto.builder()
                .title("수정 제목")
                .content("수정 내용")
                .address("address")
                .coordinateX("coordinateX")
                .coordinateY("coordinateY")
                .placeName("placeName")
                .time(LocalDateTime.now().plusDays((1)))
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
        // When
        MeetingResponseDto savedMeeting = meetingService.createMeeting(meetingRequestDto, image, savedMember);

        // Then
        assertThat(savedMeeting.getTitle()).isEqualTo("수정 제목");
        assertThat(savedMeeting.getContent()).isEqualTo("수정 내용");
    }

    @Test
    @Transactional
    @DisplayName("모임 삭제 테스트 - 정상 응답")
    public void meetingDelete() throws IOException {
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

        // When
        meetingService.meetingDelete(firstMeeting.getId(), savedMember);

        // Then

    }
}