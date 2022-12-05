package sideproject.petmeeting.member.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class MemberTest {


    @Test
    @DisplayName("Member 가 제대로 생성되는지 테스트 ")
    public void createMember() {
        Member member = Member.builder()
                .nickname("Tommy")
                .password("test")
                .email("kbs4520@naver.com")
                .image("image_url")
                .build();
        assertThat(member).isNotNull();
    }

}