package sideproject.petmeeting.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import sideproject.petmeeting.member.domain.Member;
import sideproject.petmeeting.member.dto.MemberDto;
import sideproject.petmeeting.member.repository.MemberRepository;
import sideproject.petmeeting.member.service.MemberService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith({SpringExtension.class, RestDocumentationExtension.class})
@SpringBootTest
@AutoConfigureMockMvc
class MemberControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Mock
    MemberService memberService;

    @Mock
    MemberRepository memberRepository;


    @Test
    @DisplayName("정상적인 값이 들어 왔을 때 허용")
    public void createMember() throws Exception {

        // Member 생성
        MemberDto memberDto = MemberDto.builder()
                .nickname("Tommy")
                .password("test")
                .email("test@test.com")
                .image("test-image.url")
                .build();
        Member member = Member.builder()
                .nickname(memberDto.getNickname())
                .password(memberDto.getPassword())
                .email(memberDto.getEmail())
                .image(memberDto.getImage())
                .build();

        Mockito.when(memberRepository.save(member)).thenReturn(member);
        Mockito.when(memberService.join(memberDto)).thenReturn(member);

        mockMvc.perform(post("/api/member/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaTypes.HAL_JSON)
                        .content(objectMapper.writeValueAsString(memberDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())

        ;
    }

    @Test
    @DisplayName("빈 값이 들어 왔을 때 error 발생")
    public void createMember_Empty() throws Exception {

        // Member 생성
        MemberDto memberDto = MemberDto.builder()
                .build();
        Member member = Member.builder()
                .nickname(memberDto.getNickname())
                .password(memberDto.getPassword())
                .email(memberDto.getEmail())
                .image(memberDto.getImage())
                .build();

        Mockito.when(memberRepository.save(member)).thenReturn(member);
        Mockito.when(memberService.join(memberDto)).thenReturn(member);

        mockMvc.perform(post("/api/member/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(memberDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @DisplayName("들어오면 안되는 값이 들어올 경우 error 발생")
    public void createMember_BadRequest() throws Exception {
        Member member = Member.builder()
                .id(1L)
                .nickname("Tommy")
                .password("test")
                .email("test@test.com")
                .image("test-image")
                .build();

        mockMvc.perform(post("/api/member/signup")
                .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(member)))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }
}