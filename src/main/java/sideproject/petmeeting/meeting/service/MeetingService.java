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
import sideproject.petmeeting.meeting.domain.Meeting;
import sideproject.petmeeting.meeting.dto.MeetingPageResponseDto;
import sideproject.petmeeting.meeting.dto.MeetingRequestDto;
import sideproject.petmeeting.meeting.dto.MeetingResponseDto;
import sideproject.petmeeting.meeting.repository.MeetingRepository;
import sideproject.petmeeting.member.domain.Member;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final S3Uploader s3Uploader;

    /**
     * 모임 생성
     * @param meetingRequestDto : 모임 작성에 필요한 데이터
     */
    @Transactional
    public MeetingResponseDto createMeeting(MeetingRequestDto meetingRequestDto, MultipartFile image, Member member) throws IOException {
        String imageUrl = s3Uploader.upload(image, "/meeting/image");

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
            s3Uploader.deleteImage(imageUrl);
        }

        imageUrl = s3Uploader.upload(image, "/meeting/image");
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
            s3Uploader.deleteImage(imageUrl);
        }

        meetingRepository.deleteById(meetingId);

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
                .species(meeting.getSpecies())
                .authorId(meeting.getMember().getId())
                .authorNickname(meeting.getMember().getNickname())
                .authorImageUrl(meeting.getMember().getImage())
                .createdAt(meeting.getCreatedAt())
                .modifiedAt(meeting.getModifiedAt())
                .build();
    }
}

