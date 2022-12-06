package sideproject.petmeeting.meeting.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sideproject.petmeeting.common.Response;
import sideproject.petmeeting.common.ResponseResource;
import sideproject.petmeeting.common.StatusEnum;
import sideproject.petmeeting.meeting.dto.MeetingPageResponseDto;
import sideproject.petmeeting.meeting.dto.MeetingRequestDto;
import sideproject.petmeeting.meeting.dto.MeetingResponseDto;
import sideproject.petmeeting.meeting.service.MeetingService;
import sideproject.petmeeting.security.UserDetailsImpl;

import javax.validation.Valid;
import java.io.IOException;

import static org.springframework.hateoas.MediaTypes.HAL_JSON_VALUE;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api", produces = HAL_JSON_VALUE)
public class MeetingController {

    private final MeetingService meetingService;


    /**
     * 모임 생성
     * @param meetingRequestDto : 모임 생성에 필요한 데이터
     * @param image : 모임 생성에 첨부 할 이미지
     * @return :
     */
    @PostMapping("/meeting")
    public ResponseEntity<Object> createPost(@RequestPart(value = "data") @Valid MeetingRequestDto meetingRequestDto, // @valid 객체 검증 수행
                                             @RequestPart(value = "image", required = false) @Valid MultipartFile image,
                                             @AuthenticationPrincipal UserDetailsImpl userDetails,
                                             Errors errors) throws IOException {

        if (errors.hasErrors()) {
            Response response = new Response(StatusEnum.BAD_REQUEST, "다시 시도해 주세요", errors);

            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        MeetingResponseDto meetingResponseDto = meetingService.createMeeting(meetingRequestDto, image, userDetails.getMember());

        ResponseResource responseResource = new ResponseResource(meetingResponseDto);
        responseResource.add(linkTo(MeetingController.class).withSelfRel());
        responseResource.add(linkTo(MeetingController.class).slash(meetingResponseDto.getId()).withRel("meeting-get"));
        responseResource.add(linkTo(MeetingController.class).slash(meetingResponseDto.getId()).withRel("meeting-edit"));
        responseResource.add(linkTo(MeetingController.class).slash(meetingResponseDto.getId()).withRel("meeting-delete"));

        Response response = new Response(StatusEnum.CREATED, "모임 생성 성공", responseResource);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }


    /**
     * 모임 전체 조회
     * @param pageNum : 조회할 페이지 번호
     * @return :
     */
    @GetMapping("/meeting")
    public ResponseEntity<Object> getAllMeeting(@RequestParam("page") int pageNum) {
        MeetingPageResponseDto meetingPageResponseDto = meetingService.getAllMeeting(pageNum);

        ResponseResource responseResource = new ResponseResource(meetingPageResponseDto);
        responseResource.add(linkTo(MeetingController.class).withSelfRel());

        Response response = new Response(StatusEnum.OK, "모임 조회 성공", responseResource);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    /**
     * 모임 단건 조회
     * @param meetingId : 조회할 모임 id
     * @return :
     */
    @GetMapping("/meeting/{meetingId}")
    public ResponseEntity<Object> getMeeting(@PathVariable Long meetingId) {
        MeetingResponseDto meetingResponseDto = meetingService.getMeeting(meetingId);

        ResponseResource responseResource = new ResponseResource(meetingResponseDto);
        responseResource.add(linkTo(MeetingController.class).withSelfRel());

        Response response = new Response(StatusEnum.OK, "모임 조회 성공", responseResource);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    /**
     * 모임 수정
     * @param meetingId : 수정할 모임 id
     * @return :
     */
    @PutMapping("/meeting/{meetingId}")
    public ResponseEntity<Object> updateMeeting(@PathVariable Long meetingId,
                                                @RequestPart(value = "data") @Valid MeetingRequestDto meetingRequestDto,
                                                @RequestPart(value = "image", required = false) @Valid MultipartFile image,
                                                @AuthenticationPrincipal UserDetailsImpl userDetails) throws IOException {
        MeetingResponseDto meetingResponseDto = meetingService.updateMeeting(meetingId, meetingRequestDto, image, userDetails.getMember());

        ResponseResource responseResource = new ResponseResource(meetingResponseDto);
        responseResource.add(linkTo(MeetingController.class).withSelfRel());

        Response response = new Response(StatusEnum.OK, "모임 수정 성공", responseResource);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 모임 삭제
     * @param meetingId : 삭제할 모임 id
     * @return : 삭제 완료 응답
     */
    @DeleteMapping("/meeting/{meetingId}")
    public ResponseEntity<Object> deleteMeeting(@PathVariable Long meetingId,
                                                @AuthenticationPrincipal UserDetailsImpl userDetails) throws IOException {
        meetingService.meetingDelete(meetingId, userDetails.getMember());

        ResponseResource responseResource = new ResponseResource(null);
        responseResource.add(linkTo(MeetingController.class).withSelfRel());

        Response response = new Response(StatusEnum.OK, "모임 삭제 성공", responseResource);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}

