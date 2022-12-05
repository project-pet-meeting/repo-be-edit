package sideproject.petmeeting.member.controller;

import lombok.AllArgsConstructor;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import sideproject.petmeeting.member.MemberResource;
import sideproject.petmeeting.member.domain.Member;
import sideproject.petmeeting.member.dto.MemberDto;
import sideproject.petmeeting.member.service.MemberService;
import sideproject.petmeeting.member.validator.MemberValidator;

import javax.validation.Valid;
import java.net.URI;

import static org.springframework.hateoas.MediaTypes.HAL_JSON_VALUE;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Controller
@RequestMapping(value = "api/member", produces = HAL_JSON_VALUE)
@AllArgsConstructor
public class MemberController {

    //== Dependency Injection ==//
    private final MemberValidator memberValidator;
    private final MemberService memberService;

    @PostMapping(value = "/signup")
    public ResponseEntity signup(@RequestBody @Valid MemberDto memberDto, Errors errors) {
        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(errors);
        }
        Member savedMember = memberService.join(memberDto);

        WebMvcLinkBuilder selfLinkBuilder = linkTo(MemberController.class).slash(savedMember.getId());
        URI createdUri = selfLinkBuilder.toUri();

        MemberResource memberResource = new MemberResource(savedMember);
        memberResource.add(linkTo(MemberController.class).withRel("query-events"));

        return ResponseEntity.created(createdUri).body(memberResource);
    }
}
