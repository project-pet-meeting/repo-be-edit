package sideproject.petmeeting.chat.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sideproject.petmeeting.chat.domain.ChatMessage;
import sideproject.petmeeting.chat.dto.response.ChatMessageResponseDto;
import sideproject.petmeeting.chat.repository.ChatMessageRepository;
import sideproject.petmeeting.chat.service.ChatMessageService;
import sideproject.petmeeting.common.Response;
import sideproject.petmeeting.common.ResponseResource;
import sideproject.petmeeting.common.exception.BusinessException;
import sideproject.petmeeting.member.domain.Member;
import sideproject.petmeeting.member.repository.MemberRepository;
import sideproject.petmeeting.security.TokenProvider;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static sideproject.petmeeting.common.StatusEnum.CREATED;
import static sideproject.petmeeting.common.StatusEnum.OK;
import static sideproject.petmeeting.common.exception.ErrorCode.INVALID_TOKEN;
import static sideproject.petmeeting.common.exception.ErrorCode.NEED_LOGIN;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/message")
public class ChatMessageController {
    private final TokenProvider tokenProvider;
    private final MemberRepository memberRepository;
    private final ChatMessageService chatMessageService;
    @GetMapping("/{chatRoomId}")
    public ResponseEntity getMessageList(@PathVariable String chatRoomId,
                                         HttpServletRequest httpServletRequest) {
        Response message = new Response();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));
        checkAuthentication(httpServletRequest);

        ResponseResource responseResource;
        List<ChatMessageResponseDto> chatMessageList = chatMessageService.getMessageList(chatRoomId);
        responseResource = new ResponseResource(chatMessageList);
        if (chatMessageList.isEmpty()) {
            responseResource = new ResponseResource("Empty Message");
        }

        responseResource.add(linkTo(ChatMessageController.class).withSelfRel());

        message.setStatus(OK);
        message.setMessage("메세지 조회 완료");
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
