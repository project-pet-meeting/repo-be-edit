package sideproject.petmeeting.member.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import sideproject.petmeeting.member.domain.Member;
import sideproject.petmeeting.member.dto.request.MemberDto;
import sideproject.petmeeting.member.repository.MemberRepository;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;


@SpringBootTest
@ActiveProfiles("test")
class MemberServiceTest {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    MemberService memberservice;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("Member 가 정상적으로 저장이 되는 지 확인 하는 테스트")
    public void join() throws IOException {
        // Given
        MemberDto member = MemberDto.builder()
                .nickname("Tommy")
                .password("test")
                .email("test@test.com")
                .build();
        // When
        Member savedMember = memberservice.join(member, null);
        Optional<Member> findMember = memberRepository.findById(savedMember.getId());

        // Then
        assertAll(
                () -> assertThat(savedMember.getId()).isEqualTo(findMember.get().getId()),
                () -> assertThat(passwordEncoder.matches(member.getPassword(), findMember.get().getPassword())).isTrue()
        );
    }
}