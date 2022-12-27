package sideproject.petmeeting.follow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import sideproject.petmeeting.follow.domain.Follow;
import sideproject.petmeeting.follow.domain.dto.FollowRequestDto;
import sideproject.petmeeting.follow.repository.FollowRepository;
import sideproject.petmeeting.member.domain.Member;
import sideproject.petmeeting.member.dto.request.LoginRequestDto;
import sideproject.petmeeting.member.repository.MemberRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.hateoas.MediaTypes.HAL_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static sideproject.petmeeting.member.domain.UserRole.ROLE_MEMBER;

@SpringBootTest
@Transactional
@ExtendWith({SpringExtension.class, RestDocumentationExtension.class})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FollowControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    FollowRepository followRepository;

    @BeforeEach
    void setting() {
        Member member1 = Member.builder()
                .nickname("Tommy")
                .password("test")
                .email("tommy@test.com")
                .image("test-image")
                .userRole(ROLE_MEMBER)
                .build();

        Member member2 = Member.builder()
                .nickname("Lisa")
                .password("test")
                .email("lisa@test.com")
                .image("test-image")
                .userRole(ROLE_MEMBER)
                .build();
        memberRepository.save(member1);
        memberRepository.save(member2);
    }
    @Test
    void follow() throws Exception {
        Member lisa = memberRepository.findByEmail("lisa@test.com").get();
        FollowRequestDto followRequestDto = new FollowRequestDto(lisa.getId());
        this.mockMvc.perform(post("/api/follow")
                .header("Authorization", getAccessToken())
                .contentType(APPLICATION_JSON)
                .accept(HAL_JSON)
                .content(objectMapper.writeValueAsString(followRequestDto)))
                .andExpect(status().isCreated())
                .andDo(print());
        assertThat(followRepository.findAll().size()).isEqualTo(1);
        assertThat(followRepository.findByFollowing(lisa)).isPresent();
    }

    @Test
    void unfollow() throws Exception {
        Member lisa = memberRepository.findByEmail("lisa@test.com").get();
        Member tommy = memberRepository.findByEmail("tommy@test.com").get();
        Follow follow = followRepository.save(Follow.builder()
                .follower(tommy)
                .following(lisa)
                .build());

        this.mockMvc.perform(delete("/api/follow/" + follow.getId())
                .header("Authorization", getAccessToken()))
                .andExpect(status().isOk())
                .andDo(print());

        assertThat(followRepository.findAll().size()).isEqualTo(0);
    }

    @Test
    void getFollowMembers() throws Exception {
        Member lisa = memberRepository.findByEmail("lisa@test.com").get();
        Member tommy = memberRepository.findByEmail("tommy@test.com").get();
        Follow follow = followRepository.save(Follow.builder()
                .follower(tommy)
                .following(lisa)
                .build());

        this.mockMvc.perform(get("/api/follow/follow-member")
                .header("Authorization", getAccessToken()))
                .andDo(print())
                .andExpect(status().isOk());
    }
    @Test
    void getFollower() throws Exception {
        Member lisa = memberRepository.findByEmail("lisa@test.com").get();
        Member tommy = memberRepository.findByEmail("tommy@test.com").get();
        Follow follow = followRepository.save(Follow.builder()
                .follower(lisa)
                .following(tommy)
                .build());

        this.mockMvc.perform(get("/api/follow/follower")
                        .header("Authorization", getAccessToken()))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void follow_NotFound() throws Exception{
        Member lisa = memberRepository.findByEmail("lisa@test.com").get();
        FollowRequestDto followRequestDto = new FollowRequestDto(3L);
        this.mockMvc.perform(post("/api/follow")
                        .header("Authorization", getAccessToken())
                        .contentType(APPLICATION_JSON)
                        .accept(HAL_JSON)
                        .content(objectMapper.writeValueAsString(followRequestDto)))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    void follow_BadRequest() throws Exception{
        Member lisa = memberRepository.findByEmail("lisa@test.com").get();
        this.mockMvc.perform(post("/api/follow")
                        .header("Authorization", getAccessToken())
                        .contentType(APPLICATION_JSON)
                        .accept(HAL_JSON)
                        )
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void unfollow_NotFound() throws Exception {
        Member lisa = memberRepository.findByEmail("lisa@test.com").get();
        Member tommy = memberRepository.findByEmail("tommy@test.com").get();
        Follow follow = followRepository.save(Follow.builder()
                .follower(tommy)
                .following(lisa)
                .build());

        this.mockMvc.perform(delete("/api/follow/" + (follow.getId() + 1))
                        .header("Authorization", getAccessToken()))
                .andExpect(status().isNotFound())
                .andDo(print());

        assertThat(followRepository.findAll().size()).isEqualTo(1);
    }



    private String getAccessToken() throws Exception {
        // Given
        String email = "tommy@test.com";
        String password = "test";
        LoginRequestDto loginRequestDto = new LoginRequestDto(email, password);

        // When & Then
        ResultActions perform = this.mockMvc.perform(post("/api/member/login")
                        .contentType(APPLICATION_JSON)
                        .accept(HAL_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDto)))
                .andExpect(status().isOk());

        return perform.andReturn().getResponse().getHeader("Authorization");
    }
}