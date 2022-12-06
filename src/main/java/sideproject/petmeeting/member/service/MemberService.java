package sideproject.petmeeting.member.service;

import lombok.AllArgsConstructor;
import net.bytebuddy.pool.TypePool;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import sideproject.petmeeting.member.domain.Member;
import sideproject.petmeeting.member.dto.request.MemberDto;
import sideproject.petmeeting.member.dto.request.MemberUpdateRequest;
import sideproject.petmeeting.member.repository.MemberRepository;

import java.util.Optional;

@Service
@AllArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    //== Dependency Injection ==//
    public Member join(MemberDto memberDto) throws ResponseStatusException {

        Optional<Member> optionalMember = memberRepository.findByEmail(memberDto.getEmail());
        if (optionalMember.isPresent()) {
            throw new IllegalStateException("이미 회원이 존재합니다.");
        }
        Member member = Member.builder()
                .nickname(memberDto.getNickname())
                .password(memberDto.getPassword())
                .email(memberDto.getEmail())
                .image(memberDto.getImage())
                .build();

        Member savedMember = memberRepository.save(member);
        return savedMember;

    }

    @Transactional
    public  Member update(MemberUpdateRequest memberUpdateRequest) {
        String email = memberUpdateRequest.getOriginEmail();
        Optional<Member> optionalMember = memberRepository.findByEmail(email);
        if (optionalMember.isEmpty()) {
            throw new IllegalStateException("회원이 존재하지 않습니다");
        }
        Member member = optionalMember.get();
        Member updateMember = member.update(memberUpdateRequest);
        return updateMember;
    }
}
