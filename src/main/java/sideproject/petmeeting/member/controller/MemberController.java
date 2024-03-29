package sideproject.petmeeting.member.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import sideproject.petmeeting.common.Response;
import sideproject.petmeeting.common.ResponseResource;
import sideproject.petmeeting.common.StatusEnum;
import sideproject.petmeeting.member.domain.Member;
import sideproject.petmeeting.member.domain.dto.LoginResponseDto;
import sideproject.petmeeting.member.dto.request.*;
import sideproject.petmeeting.member.dto.response.MemberDetailResponseDto;
import sideproject.petmeeting.member.dto.response.NicknameResponseDto;
import sideproject.petmeeting.member.dto.response.SignupResponseDto;
import sideproject.petmeeting.member.emailvalidation.EmailServiceImpl;
import sideproject.petmeeting.member.repository.MemberRepository;
import sideproject.petmeeting.member.service.MemberService;
import sideproject.petmeeting.member.validator.MemberValidator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
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
@RestController
public class MemberController {

    //== Dependency Injection ==//
    private final MemberValidator memberValidator;
    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final EmailServiceImpl emailService;

    @PostMapping(value = "/signup")
    public ResponseEntity signup(@RequestPart(value = "data") @Valid MemberDto memberDto,
                                 @RequestPart(value = "image", required = false) @Valid MultipartFile image,
                                 Errors errors) throws IOException {
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

            Member savedMember = memberService.join(memberDto, image);

            SignupResponseDto signupResponseDto = new SignupResponseDto(savedMember.getId());

            ResponseResource memberResource = new ResponseResource(signupResponseDto);
            memberResource.add(linkTo(MemberController.class).withSelfRel());
            memberResource.add(linkTo(MemberController.class).slash(savedMember.getId()).withRel("profile-edit"));
            memberResource.add(linkTo(MemberController.class).slash("/logout").withRel("log-out"));

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
            memberResource.add(linkTo(MemberController.class).withSelfRel());
            message.setStatus(StatusEnum.OK);
            message.setMessage("이미 존재하는 닉네임 입니다.");
            message.setData(nicknameResponseDto);
            return new ResponseEntity<>(message, headers, HttpStatus.OK);
        }
        ResponseResource responseResource = new ResponseResource(nicknameResponseDto);
        responseResource.add(linkTo(MemberController.class).withSelfRel());
        message.setStatus(StatusEnum.OK);
        message.setMessage("사용가능한 닉네임 입니다.");
        message.setData(responseResource);
        return new ResponseEntity<>(message, headers, HttpStatus.OK);
    }

    @PutMapping("/detail")
    public ResponseEntity<Object> detailMember(@RequestBody @Valid MemberDetailRequestDto memberDetailRequestDto, HttpServletRequest httpServletRequest, Errors errors, HttpServletResponse httpServletResponse) {
        Response response = new Response();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));

        if (errors.hasErrors()) {
            response.setStatus(StatusEnum.BAD_REQUEST);
            response.setMessage("다시 시도해 주세요");
            response.setData(errors);
            return new ResponseEntity<>(response, headers, BAD_REQUEST);
        }

        Member detailMember = memberService.detailMember(memberDetailRequestDto, httpServletRequest, httpServletResponse);
        ResponseResource responseResource = new ResponseResource(detailMember.getNickname());
        responseResource.add(linkTo(MemberController.class).slash("detail").withSelfRel());
        responseResource.add(linkTo(MemberController.class).slash("logout").withRel("logout"));
        response.setStatus(StatusEnum.OK);
        response.setMessage("회원 상세 정보 저장이 완료되었습니다.");
        response.setData(responseResource);
        return new ResponseEntity<>(response, headers, HttpStatus.OK);
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
            Member member = memberService.login(loginRequestDto, httpServletResponse);
            String nickname = member.getNickname()==null ? "null" : member.getNickname();
            ResponseResource responseResource = new ResponseResource(new LoginResponseDto(nickname));
            responseResource.add(linkTo(MemberController.class).withSelfRel());
            response.setStatus(StatusEnum.OK);
            response.setMessage("로그인에 성공하였습니다.");
            response.setData(responseResource);
            return new ResponseEntity<>(response, headers, HttpStatus.OK);
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(BAD_REQUEST, "다시 로그인 해주세요.", e);
        }
    }

    @PutMapping
    public ResponseEntity updateMember(@RequestBody MemberUpdateRequest memberUpdateRequest,
                                       HttpServletRequest httpServletRequest,
                                       Errors errors,
                                       HttpServletResponse httpServletResponse) {
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
        ResponseResource responseResource = new ResponseResource(updateMember.getEmail());
        responseResource.add(linkTo(MemberController.class).withSelfRel());
        responseResource.add(linkTo(MemberController.class).slash("logout").withRel("logout"));
        response.setStatus(StatusEnum.OK);
        response.setMessage("회원 수정이 완료되었습니다.");
        response.setData(responseResource);
        return new ResponseEntity<>(response, headers, HttpStatus.OK);
    }

    @DeleteMapping(value = "/logout")
    public ResponseEntity logout(HttpServletRequest httpServletRequest) {
        Response response = new Response();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));

        memberService.logout(httpServletRequest);
        ResponseResource responseResource = new ResponseResource(null);
        responseResource.add(linkTo(MemberController.class).withSelfRel());
        response.setStatus(StatusEnum.OK);
        response.setMessage("로그아웃 완료");
        response.setData(responseResource);
        return new ResponseEntity<>(response, headers, HttpStatus.OK);
    }

    /**
     * 이메일 인증
     * @param email : 검증 할 Email 주소
     */
    @PostMapping("/emailConfirm")
    public ResponseEntity emailConfirm(@RequestParam String email) throws Exception {
        Response response = new Response();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));
        String confirm = emailService.sendSimpleMessage(email);

        ResponseResource responseResource = new ResponseResource(confirm);
        responseResource.add(linkTo(MemberController.class).slash("emailConfirm").withSelfRel());
        response.setStatus(StatusEnum.OK);
        response.setMessage("Email 검증을 진행해주세요");
        response.setData(responseResource);
        return new ResponseEntity<>(response,headers,HttpStatus.OK);
    }
}
