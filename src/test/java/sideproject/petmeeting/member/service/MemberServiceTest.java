package sideproject.petmeeting.member.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import sideproject.petmeeting.member.domain.Member;
import sideproject.petmeeting.member.repository.MemberRepository;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


@SpringBootTest
class MemberServiceTest {

    @Autowired
    MemberRepository memberRepository;

    @Test
    @DisplayName("Member 가 정상적으로 저장이 되는 지 확인 하는 테스트")
    public void join() {
        // Given
        Member member = Member.builder()
                .nickname("Tommy")
                .password("test")
                .email("test@test.com")
                .image("test-image.url")
                .build();
        // When
        Member savedMember = memberRepository.save(member);

        Optional<Member> findMember = memberRepository.findById(savedMember.getId());

        // Then
        assertThat(savedMember.getId()).isEqualTo(findMember.get().getId());
    }
}