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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.headers.HeaderDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import sideproject.petmeeting.member.domain.Member;
import sideproject.petmeeting.member.dto.request.LoginRequestDto;
import sideproject.petmeeting.member.dto.request.MemberDto;
import sideproject.petmeeting.member.dto.request.MemberUpdateRequest;
import sideproject.petmeeting.member.dto.request.NicknameRequestDto;
import sideproject.petmeeting.member.repository.MemberRepository;
import sideproject.petmeeting.member.service.MemberService;
import sideproject.petmeeting.token.repository.RefreshTokenRepository;

import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;

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
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static sideproject.petmeeting.member.domain.UserRole.ROLE_MEMBER;

@ExtendWith({SpringExtension.class, RestDocumentationExtension.class})
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
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

        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
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
                .password("test2")
                .email("test2@test.com")
                .build();

        MockMultipartFile image = new MockMultipartFile(
                "images",
                "memberImage.jpeg",
                "image/jpeg",
                "<<jpeg data>>".getBytes()
        );

        String memberDtoJson = objectMapper.writeValueAsString(memberDto);
        MockMultipartFile data = new MockMultipartFile("data", "data", "application/json", memberDtoJson.getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/api/member/signup")
                        .file(image)
                        .file(data))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("data.id").exists())
                .andDo(document("member-signup",
                        requestHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content-type")
                        ),
                        requestPartFields("data",
                                fieldWithPath("email").description("email of Member"),
                                fieldWithPath("password").description("password of Member")
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

        String fileName = "memberImage";
        String contentType = "jpeg";
        String filePath = "src/test/resources/testImage/" + fileName + "." + contentType;
        FileInputStream fileInputStream = new FileInputStream(filePath);
        MockMultipartFile image = new MockMultipartFile("images",
                fileName + "." + contentType,
                contentType,
                fileInputStream);
        String memberDtoJson = objectMapper.writeValueAsString(memberDto);
        MockMultipartFile data = new MockMultipartFile("data", "data", "application/json", memberDtoJson.getBytes(StandardCharsets.UTF_8));



        mockMvc.perform(multipart("/api/member/signup")
                        .file(image)
                        .file(data))
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

        String fileName = "memberImage";
        String contentType = "jpeg";
        String filePath = "src/test/resources/testImage/" + fileName + "." + contentType;
        FileInputStream fileInputStream = new FileInputStream(filePath);
        MockMultipartFile image = new MockMultipartFile("images",
                fileName + "." + contentType,
                contentType,
                fileInputStream);
        String memberDtoJson = objectMapper.writeValueAsString(member);
        MockMultipartFile data = new MockMultipartFile("data", "data", "application/json", memberDtoJson.getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/api/member/signup")
                        .file(image)
                        .file(data))
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
                .password("test")
                .email("test@test.com")
                .build();

        String fileName = "memberImage";
        String contentType = "jpeg";
        String filePath = "src/test/resources/testImage/" + fileName + "." + contentType;
        FileInputStream fileInputStream = new FileInputStream(filePath);
        MockMultipartFile image = new MockMultipartFile("images",
                fileName + "." + contentType,
                contentType,
                fileInputStream);
        String memberDtoJson = objectMapper.writeValueAsString(memberDto);
        MockMultipartFile data = new MockMultipartFile("data", "data", "application/json", memberDtoJson.getBytes(StandardCharsets.UTF_8));


        mockMvc.perform(multipart("/api/member/signup")
                        .file(image)
                        .file(data))
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
                .andDo(document("nickname-duplicate",
                        requestHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content-type")
                        ),
                        requestFields(
                                fieldWithPath("nickname").description("nickname to check duplication")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content-type")
                        ),
                        responseFields(
                                fieldWithPath("status").description("status of response"),
                                fieldWithPath("message").description("message of response"),
                                fieldWithPath("data.nickname").description("nickname to check duplication"),
                                fieldWithPath("data.links[0].rel").description("relation of url"),
                                fieldWithPath("data.links[0].href").description("relational link")
                        )))
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
                .andExpect(status().isOk())
                .andDo(document("login",
                        requestHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content-type")
                        ),
                        requestFields(
                                fieldWithPath("email").description("email of Member"),
                                fieldWithPath("password").description("password of Member")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content-type"),
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Access Token"),
                                headerWithName("RefreshToken").description("Refresh Token")
                        ),
                        responseFields(
                                fieldWithPath("status").description("status of response"),
                                fieldWithPath("message").description("message of response"),
                                fieldWithPath("data.nickname").description("nickname of member"),
                                fieldWithPath("data.links[0].rel").description("relation of url"),
                                fieldWithPath("data.links[0].href").description("relational link")
                        )));

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
                .build();

        // When & Then
        mockMvc.perform(put("/api/member")
                        .header("Authorization", getAccessToken())
                        .contentType(APPLICATION_JSON)
                        .accept(HAL_JSON)
                        .content(objectMapper.writeValueAsString(memberUpdateRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("member edit",
                        requestHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content-type")
                        ),
                        requestFields(
                                fieldWithPath("nickname").description("nickname to edit"),
                                fieldWithPath("password").description("password to edit"),
                                fieldWithPath("email").description("email to edit"),
                                fieldWithPath("location").description("location to edit")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content-type"),
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Access Token"),
                                headerWithName("RefreshToken").description("Refresh Token")
                        ),
                        responseFields(
                                fieldWithPath("status").description("status of response"),
                                fieldWithPath("message").description("message of response"),
                                fieldWithPath("data.object").description("email of member"),
                                fieldWithPath("data.links[0].rel").description("relation of url"),
                                fieldWithPath("data.links[0].href").description("relational link")
                        )))
        ;
        assertAll(
                () -> assertThat(memberRepository.findByEmail("test@naver.com")).isPresent(),
                () -> assertThat(memberRepository.findByEmail("test@test.com")).isEmpty()
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
                .andDo(document("logout",
                        requestHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content-type")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content-type")
                        ),
                        responseFields(
                                fieldWithPath("status").description("status of response"),
                                fieldWithPath("message").description("message of response"),
                                fieldWithPath("data.links[0].rel").description("relation of url"),
                                fieldWithPath("data.links[0].href").description("relational link")
                        )))
        ;
        assertThat(refreshTokenRepository.findAll().size()).isEqualTo(0);
    }

    @Test
    @DisplayName("Email 검증 로직 ")
    public void EmailConfirm() throws Exception {
        this.mockMvc.perform(RestDocumentationRequestBuilders.post("/api/member/emailConfirm")
                        .param("email", "kbs4520@naver.com")
                        .contentType(APPLICATION_JSON)
                        .accept(HAL_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("data.object").exists())
                .andDo(document("email-validation",
                        requestParameters(
                                parameterWithName("email").description("email to send validation code")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content-type")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content-type")
                        ),
                        responseFields(
                                fieldWithPath("status").description("status of response"),
                                fieldWithPath("message").description("message of response"),
                                fieldWithPath("data.object").description("validation code"),
                                fieldWithPath("data.links[0].rel").description("relation of url"),
                                fieldWithPath("data.links[0].href").description("relational link")
                        )))
        ;


    }
}
