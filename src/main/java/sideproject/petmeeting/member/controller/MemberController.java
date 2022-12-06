package sideproject.petmeeting.member.controller;

import lombok.AllArgsConstructor;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import sideproject.petmeeting.common.Response;
import sideproject.petmeeting.common.ResponseResource;
import sideproject.petmeeting.common.StatusEnum;
import sideproject.petmeeting.member.domain.Member;
import sideproject.petmeeting.member.dto.request.LoginRequestDto;
import sideproject.petmeeting.member.dto.request.MemberDto;
import sideproject.petmeeting.member.dto.request.MemberUpdateRequest;
import sideproject.petmeeting.member.dto.request.NicknameRequestDto;
import sideproject.petmeeting.member.dto.response.NicknameResponseDto;
import sideproject.petmeeting.member.dto.response.SignupResponseDto;
import sideproject.petmeeting.member.repository.MemberRepository;
import sideproject.petmeeting.member.service.MemberService;
import sideproject.petmeeting.member.validator.MemberValidator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.springframework.hateoas.MediaTypes.HAL_JSON_VALUE;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static sideproject.petmeeting.common.StatusEnum.CREATED;

@Controller
@RequestMapping(value = "/api/member", produces = HAL_JSON_VALUE)
@AllArgsConstructor
public class MemberController {

    //== Dependency Injection ==//
    private final MemberValidator memberValidator;
    private final MemberService memberService;
    private final MemberRepository memberRepository;

    @PostMapping(value = "/signup")
    public ResponseEntity signup(@RequestBody @Valid MemberDto memberDto, Errors errors){
        try {
            Response response = new Response();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));

            if (errors.hasErrors()) {
                response.setStatus(StatusEnum.BAD_REQUEST);
                response.setMessage("다시 시도해주세요");
                response.setData(errors);
                return new ResponseEntity<>(response, headers, BAD_REQUEST);
            }

            Member savedMember = memberService.join(memberDto);

            WebMvcLinkBuilder selfLinkBuilder = linkTo(MemberController.class).slash(savedMember.getId());
            URI createdUri = selfLinkBuilder.toUri();

            SignupResponseDto signupResponseDto = new SignupResponseDto(savedMember.getId());

            ResponseResource memberResource = new ResponseResource(signupResponseDto);
            memberResource.add(linkTo(MemberController.class).withRel("query-events"));

            response.setStatus(CREATED);
            response.setMessage("회원 가입 성공");
            response.setData(memberResource);
            return new ResponseEntity<>(response, headers, HttpStatus.CREATED);
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(CONFLICT, "이미 회원이 존재합니다.", e);
        }
    }

    @PostMapping(value = "/nickname")
    public ResponseEntity checkDuplication(@RequestBody @Valid NicknameRequestDto nicknameRequestDto, Errors errors) {
        Response message = new Response();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));

        NicknameResponseDto nicknameResponseDto = new NicknameResponseDto(nicknameRequestDto.getNickname());

        if (errors.hasErrors()) {
            message.setStatus(StatusEnum.BAD_REQUEST);
            message.setMessage("다시 시도해 주세요");
            message.setData(errors);
            return new ResponseEntity<>(message, headers, HttpStatus.BAD_REQUEST);
        }
        Optional<Member> findMember = memberRepository.findByNickname(nicknameRequestDto.getNickname());
        if (findMember.isPresent()) {
            ResponseResource memberResource = new ResponseResource("이미 존재하는 닉네임 입니다.");

            message.setStatus(StatusEnum.OK);
            message.setMessage("이미 존재하는 닉네임 입니다.");
            message.setData(nicknameResponseDto);
            return new ResponseEntity<>(message, headers, HttpStatus.OK);
        }
        message.setStatus(StatusEnum.OK);
        message.setMessage("사용가능한 닉네임 입니다.");
        message.setData(nicknameResponseDto);
        return new ResponseEntity<>(message, headers, HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody LoginRequestDto loginRequestDto, Errors errors, HttpServletResponse httpServletResponse) {
        try {
            Response response = new Response();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));

            if (errors.hasErrors()) {
                response.setStatus(StatusEnum.BAD_REQUEST);
                response.setMessage("다시 시도해 주세요");
                response.setData(errors);
                return new ResponseEntity<>(response, headers, BAD_REQUEST);
            }
            memberService.login(loginRequestDto, httpServletResponse);
            response.setStatus(StatusEnum.OK);
            response.setMessage("로그인에 성공하였습니다.");
            response.setData(loginRequestDto.getEmail());
            return new ResponseEntity<>(response, headers, HttpStatus.OK);
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(BAD_REQUEST, "다시 로그인 해주세요.", e);
        }
    }

    @PutMapping
    public ResponseEntity updateMember(@RequestBody MemberUpdateRequest memberUpdateRequest, HttpServletRequest httpServletRequest, Errors errors, HttpServletResponse httpServletResponse) {
        Response response = new Response();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));

        if (errors.hasErrors()) {
            response.setStatus(StatusEnum.BAD_REQUEST);
            response.setMessage("다시 시도해 주세요");
            response.setData(errors);
            return new ResponseEntity<>(response, headers, BAD_REQUEST);
        }

        Member updateMember = memberService.update(memberUpdateRequest, httpServletRequest, httpServletResponse);
        response.setStatus(StatusEnum.OK);
        response.setMessage("회원 수정이 완료되었습니다.");
        response.setData(updateMember.getEmail());
        return new ResponseEntity<>(response, headers, HttpStatus.OK);
    }

    @DeleteMapping(value = "/logout")
    public ResponseEntity logout(HttpServletRequest httpServletRequest) {
        Response response = new Response();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));

        memberService.logout(httpServletRequest);
        response.setStatus(StatusEnum.OK);
        response.setMessage("로그아웃 완료");
        response.setData(null);
        return new ResponseEntity<>(response, headers, HttpStatus.OK);
    }

}
