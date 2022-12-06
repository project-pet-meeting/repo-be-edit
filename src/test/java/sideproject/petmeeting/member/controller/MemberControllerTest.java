package sideproject.petmeeting.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import sideproject.petmeeting.member.domain.Member;
import sideproject.petmeeting.member.dto.request.LoginRequestDto;
import sideproject.petmeeting.member.dto.request.MemberDto;
import sideproject.petmeeting.member.dto.request.MemberUpdateRequest;
import sideproject.petmeeting.member.dto.request.NicknameRequestDto;
import sideproject.petmeeting.member.repository.MemberRepository;
import sideproject.petmeeting.member.service.MemberService;
import sideproject.petmeeting.token.repository.RefreshTokenRepository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.hateoas.MediaTypes.HAL_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.modifyUris;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static sideproject.petmeeting.member.domain.UserRole.ROLE_MEMBER;

@ExtendWith({SpringExtension.class, RestDocumentationExtension.class})
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class MemberControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    MemberService memberService;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    RefreshTokenRepository refreshTokenRepository;

    @BeforeEach
    public void setup(WebApplicationContext webApplicationContext,
                      RestDocumentationContextProvider restDocumentationContextProvider) {
        refreshTokenRepository.deleteAll();
        memberRepository.deleteAll();

        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
//                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .apply(documentationConfiguration(restDocumentationContextProvider)
                        .operationPreprocessors()
                        .withRequestDefaults(modifyUris().host("tommy.me").removePort(), prettyPrint())
                        .withResponseDefaults(modifyUris().host("tommy.me").removePort(), prettyPrint()))
                .alwaysDo(print())
                .build();
    }


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


        mockMvc.perform(post("/api/member/signup")
                        .contentType(APPLICATION_JSON)
                        .accept(HAL_JSON)
                        .content(objectMapper.writeValueAsString(memberDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("data.id").exists())
                .andDo(document("member-signup",
                        requestHeaders(
                                headerWithName(HttpHeaders.ACCEPT).description("accept-header"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content-type")
                        ),
                        requestFields(
                                fieldWithPath("nickname").description("member nickname"),
                                fieldWithPath("password").description("member password"),
                                fieldWithPath("email").description("member email"),
                                fieldWithPath("image").description("member image")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content-type")
                        ),
                        responseFields(
                                fieldWithPath("status").description("status of response"),
                                fieldWithPath("message").description("message of response"),
                                fieldWithPath("data.id").description("id of member"),
                                fieldWithPath("data.links[0].rel").description("relation of url"),
                                fieldWithPath("data.links[0].href").description("relational link")
                        )
                ))
        ;

        assertThat(memberRepository.findByEmail(memberDto.getEmail())).isPresent();
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


        mockMvc.perform(post("/api/member/signup")
                        .contentType(APPLICATION_JSON)
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
                        .contentType(APPLICATION_JSON)
                        .accept(HAL_JSON)
                        .content(objectMapper.writeValueAsString(member)))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @DisplayName("이미 존재하는 email 로 가입시 error 발생")
    public void existMEmber_409Error() throws Exception {
        // Given
        Member member = Member.builder()
                .id(1L)
                .nickname("Tommy")
                .password("test")
                .email("test@test.com")
                .image("test-image")
                .build();
        memberRepository.save(member);

        MemberDto memberDto = MemberDto.builder()
                .nickname("Tommy")
                .password("test")
                .email("test@test.com")
                .image("test-image.url")
                .build();


        mockMvc.perform(post("/api/member/signup")
                        .contentType(APPLICATION_JSON)
                        .accept(HAL_JSON)
                        .content(objectMapper.writeValueAsString(memberDto)))
                .andDo(print())
                .andExpect(status().isConflict())
        ;
    }

    @Test
    @DisplayName("닉네임 중복 검사 테스트")
    public void checkDuplication() throws Exception {

        NicknameRequestDto nickname = NicknameRequestDto.builder()
                .nickname("Tommy").build();
        assertThat(nickname).isNotNull();

        mockMvc.perform(post("/api/member/nickname")
                        .contentType(APPLICATION_JSON)
                        .accept(HAL_JSON)
                        .content(objectMapper.writeValueAsString(nickname)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("message").isString())
        ;
    }

    @Test
    @DisplayName("회원 로그인 테스트")
    public void login_OK() throws Exception {
        // Given
        Member member = Member.builder()
                .id(1L)
                .nickname("Tommy")
                .password("test")
                .email("test@test.com")
                .image("test-image")
                .userRole(ROLE_MEMBER)
                .build();
        memberRepository.save(member);


        String email = "test@test.com";
        String password = "test";
        LoginRequestDto loginRequestDto = new LoginRequestDto(email, password);

        // When & Then
        ResultActions perform = this.mockMvc.perform(post("/api/member/login")
                        .contentType(APPLICATION_JSON)
                        .accept(HAL_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDto)))
                .andDo(print())
                .andExpect(status().isOk());

        assertAll(
                () -> assertThat(perform.andReturn().getResponse().getHeader("Authorization")).isNotNull(),
                () -> assertThat(perform.andReturn().getResponse().getHeader("RefreshToken")).isNotNull()
        );
    }

    @Test
    @DisplayName("회원 로그인 시 값 누락시 에러 처리")
    public void login_Error() throws Exception {
        // Given
        Member member = Member.builder()
                .id(1L)
                .nickname("Tommy")
                .password("test")
                .email("test@test.com")
                .image("test-image")
                .userRole(ROLE_MEMBER)
                .build();
        memberRepository.save(member);


        String email = "";
        String password = "test";
        LoginRequestDto loginRequestDto = new LoginRequestDto(email, password);

        // When & Then
        ResultActions perform = this.mockMvc.perform(post("/api/member/login")
                        .contentType(APPLICATION_JSON)
                        .accept(HAL_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());

    }

    private String getAccessToken() throws Exception {
        // Given
        Member member = Member.builder()
                .id(1L)
                .nickname("Tommy")
                .password("test")
                .email("test@test.com")
                .image("test-image")
                .userRole(ROLE_MEMBER)
                .build();
        memberRepository.save(member);

        String email = "test@test.com";
        String password = "test";
        LoginRequestDto loginRequestDto = new LoginRequestDto(email, password);

        // When & Then
        ResultActions perform = this.mockMvc.perform(post("/api/member/login")
                        .contentType(APPLICATION_JSON)
                        .accept(HAL_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDto)))
                .andDo(print())
                .andExpect(status().isOk());
        assertThat(refreshTokenRepository.findAll().size()).isEqualTo(1);

        return perform.andReturn().getResponse().getHeader("Authorization");
    }


    @Test
    @DisplayName("회원 정보 수정 테스트")
    public void updateMember() throws Exception {
        // Given
        MemberUpdateRequest memberUpdateRequest = MemberUpdateRequest.builder()
                .nickname("TommyKim")
                .password("test2")
                .email("test@naver.com")
                .image("test-image2")
                .build();

        // When & Then
        mockMvc.perform(put("/api/member")
                        .header("Authorization", getAccessToken())
                        .contentType(APPLICATION_JSON)
                        .accept(HAL_JSON)
                        .content(objectMapper.writeValueAsString(memberUpdateRequest)))
                .andDo(print())
                .andExpect(status().isOk())
        ;
        assertAll(
                () -> assertThat(memberRepository.findByEmail("test@naver.com")).isPresent(),
                () -> assertThat(memberRepository.findByEmail("test@test.com")).isEmpty(),
                () -> assertThat(refreshTokenRepository.findAll().size()).isEqualTo(1)
        );
    }

    @Test
    @DisplayName("회원정보 수정 시 잘못된 값이 올 경우 에러 남")
    public void updateMember_BadRequest() throws Exception {
        // Given
        MemberUpdateRequest memberUpdateRequest = MemberUpdateRequest.builder()
                .nickname("TommyKim")
                .password("test2")
                .email("test@naver.com")
                .image("test-image2")
                .build();

        // When & Then
        mockMvc.perform(put("/api/member")
                        .header("Authorization", getAccessToken())
                        .contentType(APPLICATION_JSON)
                        .accept(HAL_JSON)
                        .content(objectMapper.writeValueAsString(memberUpdateRequest)))
                .andExpect(status().isOk())
        ;
        assertAll(
                () -> assertThat(memberRepository.findByEmail("test@naver.com")).isPresent(),
                () -> assertThat(memberRepository.findByEmail("test@test.com")).isEmpty()
        );
    }

    @Test
    @DisplayName("정상적인 로그아웃 처리")
    public void logOut() throws Exception {
        //
        mockMvc.perform(delete("/api/member/logout")
                        .header("Authorization", getAccessToken())
                        .contentType(APPLICATION_JSON)
                        .accept(HAL_JSON))
                .andExpect(status().isOk())
                .andDo(print())
        ;
        assertThat(refreshTokenRepository.findAll().size()).isEqualTo(0);
    }
}