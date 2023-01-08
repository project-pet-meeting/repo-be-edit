package sideproject.petmeeting.chat.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import sideproject.petmeeting.chat.domain.ChatRoom;
import sideproject.petmeeting.chat.dto.request.ChatRoomRequestDto;
import sideproject.petmeeting.chat.dto.response.ChatRoomResponseDto;
import sideproject.petmeeting.chat.service.ChatRoomService;
import sideproject.petmeeting.common.Response;
import sideproject.petmeeting.common.ResponseResource;
import sideproject.petmeeting.common.StatusEnum;
import sideproject.petmeeting.common.exception.BusinessException;
import sideproject.petmeeting.member.domain.Member;
import sideproject.petmeeting.member.repository.MemberRepository;
import sideproject.petmeeting.security.TokenProvider;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static sideproject.petmeeting.common.StatusEnum.CREATED;
import static sideproject.petmeeting.common.StatusEnum.OK;
import static sideproject.petmeeting.common.exception.ErrorCode.INVALID_TOKEN;
import static sideproject.petmeeting.common.exception.ErrorCode.NEED_LOGIN;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatController {
    private final ChatRoomService chatRoomService;
    private final TokenProvider tokenProvider;
    private final MemberRepository memberRepository;

    @PostMapping("/{meetingId}")
    public ResponseEntity createChatRoom(@PathVariable Long meetingId,
                                         @RequestBody @Valid ChatRoomRequestDto chatRoomRequestDto,
                                         HttpServletRequest httpServletRequest,
                                         Errors errors) {
        Response message = new Response();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));

        if (errors.hasErrors()) {
            message.setStatus(StatusEnum.BAD_REQUEST);
            message.setMessage("다시 시도해주세요");
            message.setData(errors);
            return new ResponseEntity<>(message, headers, BAD_REQUEST);
        }
        // Token 검증
        Member member = checkAuthentication(httpServletRequest);
        ChatRoom chatRoom = chatRoomService.createChatRoom(member, meetingId, chatRoomRequestDto);

        ChatRoomResponseDto chatRoomResponseDto = ChatRoomResponseDto.builder()
                .id(chatRoom.getId())
                .meetingId(chatRoom.getMeeting().getId())
                .roomName(chatRoom.getRoomName())
                .build();

        ResponseResource responseResource = new ResponseResource(chatRoomResponseDto);
        responseResource.add(linkTo(ChatController.class).withSelfRel());

        message.setStatus(CREATED);
        message.setMessage("채팅방 생성이 완료되었습니다");
        message.setData(responseResource);
        return new ResponseEntity(message, headers, HttpStatus.CREATED);

    }

    @GetMapping
    public ResponseEntity getChatRoomList(HttpServletRequest httpServletRequest
                                          ) {
        Response message = new Response();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));


        Member member = checkAuthentication(httpServletRequest);
        List<ChatRoomResponseDto> chatRoomList = chatRoomService.getChatRoomList();
        ResponseResource responseResource = new ResponseResource(chatRoomList);
        responseResource.add(linkTo(ChatController.class).withSelfRel());

        message.setStatus(OK);
        message.setMessage("채팅방 조회 완료");
        message.setData(responseResource);
        return new ResponseEntity(message, headers, HttpStatus.OK);
    }

    private Member checkAuthentication(HttpServletRequest httpServletRequest) {
        if (httpServletRequest.getHeader("Authorization") == null || httpServletRequest.getHeader("Authorization").isEmpty()) {
            throw new BusinessException("로그인이 필요합니다.", NEED_LOGIN);
        }
        String accessToken = httpServletRequest.getHeader("Authorization").substring(7);
        tokenProvider.validateToken(accessToken);
        Member member = memberRepository.findByEmail(tokenProvider.getUserEmailByToken(accessToken)).orElse(null);
        if (member == null) {
            throw new BusinessException("올바르지 않은 토큰입니다.", INVALID_TOKEN);
        }
        return member;
    }
}
