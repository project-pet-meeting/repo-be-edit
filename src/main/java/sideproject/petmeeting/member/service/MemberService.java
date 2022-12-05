package sideproject.petmeeting.member.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import sideproject.petmeeting.member.domain.Member;
import sideproject.petmeeting.member.dto.MemberDto;
import sideproject.petmeeting.member.repository.MemberRepository;

@Service
@AllArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    //== Dependency Injection ==//
    public Member join(MemberDto memberDto) {
        Member member = Member.builder()
                .nickname(memberDto.getNickname())
                .password(memberDto.getPassword())
                .email(memberDto.getEmail())
                .image(memberDto.getImage())
                .build();

        Member savedMember = memberRepository.save(member);
        return savedMember;

    }
}
