package sideproject.petmeeting.meeting.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import sideproject.petmeeting.common.S3Uploader;
import sideproject.petmeeting.common.exception.BusinessException;
import sideproject.petmeeting.common.exception.ErrorCode;
import sideproject.petmeeting.meeting.domain.Attendance;
import sideproject.petmeeting.meeting.domain.Meeting;
import sideproject.petmeeting.meeting.dto.AttendanceResponseDto;
import sideproject.petmeeting.meeting.dto.MeetingPageResponseDto;
import sideproject.petmeeting.meeting.dto.MeetingRequestDto;
import sideproject.petmeeting.meeting.dto.MeetingResponseDto;
import sideproject.petmeeting.meeting.repository.AttendanceRepository;
import sideproject.petmeeting.meeting.repository.MeetingRepository;
import sideproject.petmeeting.member.domain.Member;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final AttendanceRepository attendanceRepository;
    private final S3Uploader s3Uploader;

    /**
     * 모임 생성
     * @param meetingRequestDto : 모임 작성에 필요한 데이터
     */
    @Transactional
    public MeetingResponseDto createMeeting(MeetingRequestDto meetingRequestDto, MultipartFile image, Member member) throws IOException {
        String imageUrl = s3Uploader.upload(image, "meeting/image");

        Meeting meeting = Meeting.builder()
                .title(meetingRequestDto.getTitle())
                .content(meetingRequestDto.getContent())
                .imageUrl(imageUrl)
                .address(meetingRequestDto.getAddress())
                .coordinateX(meetingRequestDto.getCoordinateX())
                .coordinateY(meetingRequestDto.getCoordinateY())
                .placeName(meetingRequestDto.getPlaceName())
                .time(meetingRequestDto.getTime())
                .recruitNum(meetingRequestDto.getRecruitNum())
                .species(meetingRequestDto.getSpecies())
                .member(member)
                .build();
        meetingRepository.save(meeting);

        Attendance attendance = Attendance.builder()
                .meeting(meeting)
                .member(member)
                .build();
        attendanceRepository.save(attendance);

        Integer count = attendanceRepository.countByMeetingId(meeting.getId());
        meeting.countNum(count);

        return getMeetingResponseDto(meeting);

    }


    /**
     * 모임 전체 조회
     * @param pageNum : 조회할 페이지 번호
     * @return : 해당 페이지 번호의 전체 모임, 페이지 정보
     */
    @Transactional(readOnly = true)
    public MeetingPageResponseDto getAllMeeting(int pageNum) {
        Pageable pageRequest = PageRequest.of(pageNum, 15, Sort.by("modifiedAt").descending());

        Page<Meeting> meetingPage = meetingRepository.findAll(pageRequest);

        List<Meeting> content = meetingPage.getContent();

        List<MeetingResponseDto> meetingResponseDtoList = new ArrayList<>();
        for (Meeting meeting : content) {
            meetingResponseDtoList.add(
                    getMeetingResponseDto(meeting)
            );
        }

        MeetingPageResponseDto meetingPageResponseDto = MeetingPageResponseDto.builder()
                .meetingList(meetingResponseDtoList)
                .totalPage(meetingPage.getTotalPages() - 1)
                .currentPage(pageNum)
                .isFirstPage(meetingPage.isFirst())
                .hasNextPage(meetingPage.hasNext())
                .hasPreviousPage(meetingPage.hasPrevious())
                .build();

        return meetingPageResponseDto;

    }

    /**
     * 모임 단건 조회
     * @param meetingId : 조회할 모임 id
     * @return : 조회한 모임
     */
    @Transactional(readOnly = true)
    public MeetingResponseDto getMeeting(Long meetingId) {
        Meeting meeting = meetingRepository.findMeetingIdFetchJoin(meetingId).orElseThrow(
                () -> new BusinessException("존재하지 않는 모임 id 입니다.", ErrorCode.MEETING_NOT_EXIST)
        );

        return getMeetingResponseDto(meeting);
    }


    /**
     * 모임 수정
     * @param meetingId : 수정할 모임 id
     * @param meetingRequestDto : 수정할 데이터
     * @param image : 수정할 이미지 파일
     * @return :
     * @throws IOException : IOException 예외 처리
     */
    @Transactional
    public MeetingResponseDto updateMeeting(Long meetingId, MeetingRequestDto meetingRequestDto, MultipartFile image, Member member) throws IOException {
        Meeting meeting = meetingRepository.findById(meetingId).orElseThrow(
                () -> new BusinessException("존재하지 않는 모임 id 입니다.", ErrorCode.MEETING_NOT_EXIST)
        );

        if (!meeting.getMember().getId().equals(member.getId())) {
            throw new BusinessException("수정 권한이 없습니다.", ErrorCode.HANDLE_ACCESS_DENIED);
        }

        String imageUrl = meeting.getImageUrl();

        // 이미지 존재 시 삭제 후 업로드
        if (imageUrl != null) {
            s3Uploader.deleteImage(imageUrl, "meeting/image");
        }

        imageUrl = s3Uploader.upload(image, "meeting/image");
        meeting.update(meetingRequestDto, imageUrl);

        return getMeetingResponseDto(meeting);
    }


    /**
     * 모임 삭제
     * @param meetingId : 삭제할 모임 id
     * @throws IOException : 삭제할 모임의 image 파일명 인코딩 예외 처리, UnsupportedEncodingException
     */
    @Transactional
    public void meetingDelete(Long meetingId, Member member) throws IOException {
        Meeting meeting = meetingRepository.findById(meetingId).orElseThrow(
                () -> new BusinessException("존재하지 않는 모임 id 입니다.", ErrorCode.MEETING_NOT_EXIST)
        );

        if (!meeting.getMember().getId().equals(member.getId())) {
            throw new BusinessException("삭제 권한이 없습니다.", ErrorCode.HANDLE_ACCESS_DENIED);
        }

        String imageUrl = meeting.getImageUrl();

        if (imageUrl != null) {
            s3Uploader.deleteImage(imageUrl, "meeting/image");
        }

        meetingRepository.deleteById(meetingId);

    }

    /**
     * 모임 참석
     * @param meetingId: 참석할 모임 id
     * @param member: 사용자 정보
     * @return : 참석한 모임 정보
     */
    @Transactional
    public MeetingResponseDto addAttendance(Long meetingId, Member member) {
        Meeting meeting = meetingRepository.findById(meetingId).orElseThrow(
                () -> new BusinessException("존재하지 않는 모임 id 입니다.", ErrorCode.MEETING_NOT_EXIST)
        );

        if (attendanceRepository.findByMeetingAndMember(meeting, member).isPresent()) {
            throw new BusinessException("이미 참여중인 모임입니다.", ErrorCode.ALREADY_ATTENDANCE_MEETING);
        }

        if (attendanceRepository.countByMeetingId(meetingId) >= meeting.getRecruitNum()) {
            throw new BusinessException("모집 인원이 마감되었습니다.", ErrorCode.MEETING_RECRUIT_FULL);
        }

        Attendance attendance = Attendance.builder()
                .meeting(meeting)
                .member(member)
                .build();
        attendanceRepository.save(attendance);

        Integer count = attendanceRepository.countByMeetingId(meetingId);
        meeting.countNum(count);

        return getMeetingResponseDto(meeting);
    }


    /**
     * 모임 참석 취소
     * @param meetingId: 취소할 모임 id
     * @param member: 사용자 정보
     */
    @Transactional
    public void deleteAttendance(Long meetingId, Member member) {
        Meeting meeting = meetingRepository.findById(meetingId).orElseThrow(
                () -> new BusinessException("존재하지 않는 모임 id 입니다.", ErrorCode.MEETING_NOT_EXIST)
        );

        Optional<Attendance> attendanceOptional = attendanceRepository.findByMeetingAndMember(meeting, member);
        if (attendanceOptional.isEmpty()) {
            throw new BusinessException("참석 중인 모임이 아닙니다.", ErrorCode.ATTENDANCE_NOT_EXIST);
        }

        attendanceRepository.delete(attendanceOptional.get());

        Integer count = attendanceRepository.countByMeetingId(meetingId);
        meeting.countNum(count);

//        if (meeting.getMember().getId().equals(member.getId())) {
//            meetingDelete(meetingId, member);
//        }

    }


    /**
     * 모임 참석자 조회
     * @param meetingId: 참석자 조회할 모임 id
     * @param member:
     * @return : 모임 참석자
     */
    public AttendanceResponseDto getAttendance(Long meetingId, Member member) {
        Meeting meeting = meetingRepository.findById(meetingId).orElseThrow(
                () -> new BusinessException("존재하지 않는 모임 id 입니다.", ErrorCode.MEETING_NOT_EXIST)
        );

        Optional<Attendance> attendanceOptional = attendanceRepository.findByMeetingAndMember(meeting, member);
        if (attendanceOptional.isEmpty()) {
            throw new BusinessException("참석하지 않은 모임의 참석 리스트 조회 불가.", ErrorCode.ATTENDANCE_LIST_ACCESS_DENIED);
        }

        // 어텐던스 리포지토리에서 미팅 아이디로 검색한 멤버 list 가져오기
        List<Attendance> memberList = attendanceRepository.findMemberFetchJoin(meetingId);

        List<AttendanceResponseDto> attendanceListDto = new ArrayList<>();
        for(Attendance attendance : memberList) {
            attendanceListDto.add(
                    AttendanceResponseDto.builder()
                            .id(attendance.getId())
                            .nickname(attendance.getMember().getNickname())
                            .image(attendance.getMember().getImage())
                            .location(attendance.getMember().getLocation())
                            .build()
            );
        }

        return AttendanceResponseDto.builder()
                .attendanceList(attendanceListDto)
                .build();
    }


    /**
     * meeting 데이터를 meetingResponseDto 로 build
     * @param meeting : meeting 데이터
     * @return : 응답 데이터 meetingResponseDto
     */
    private MeetingResponseDto getMeetingResponseDto(Meeting meeting) {

        return MeetingResponseDto.builder()
                .id(meeting.getId())
                .title(meeting.getTitle())
                .content(meeting.getContent())
                .imageUrl(meeting.getImageUrl())
                .address(meeting.getAddress())
                .coordinateX(meeting.getCoordinateX())
                .coordinateY(meeting.getCoordinateY())
                .placeName(meeting.getPlaceName())
                .time(meeting.getTime())
                .recruitNum(meeting.getRecruitNum())
                .currentNum(meeting.getCurrentNum())
                .species(meeting.getSpecies())
                .authorId(meeting.getMember().getId())
                .authorNickname(meeting.getMember().getNickname())
                .authorImageUrl(meeting.getMember().getImage())
                .createdAt(meeting.getCreatedAt())
                .modifiedAt(meeting.getModifiedAt())
                .build();
    }
}
