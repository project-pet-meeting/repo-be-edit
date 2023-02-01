package sideproject.petmeeting.pet.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import sideproject.petmeeting.member.domain.Member;
import sideproject.petmeeting.member.dto.request.LoginRequestDto;
import sideproject.petmeeting.member.repository.MemberRepository;
import sideproject.petmeeting.pet.domain.Pet;
import sideproject.petmeeting.pet.dto.PetRequestDto;
import sideproject.petmeeting.pet.repository.PetRepository;
import sideproject.petmeeting.token.repository.RefreshTokenRepository;

import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.hateoas.MediaTypes.HAL_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.modifyUris;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static sideproject.petmeeting.member.domain.UserRole.ROLE_MEMBER;

@ExtendWith({SpringExtension.class, RestDocumentationExtension.class})
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@TestMethodOrder(value = MethodOrderer.OrderAnnotation.class)
@Slf4j
class PetControllerTest {

    @Autowired
    WebApplicationContext webApplicationContext;
    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    private PetRepository petRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;


    public static final String USERNAME = "petController@Username.com";
    public static final String PASSWORD = "password";

    @BeforeEach
    public void setup(RestDocumentationContextProvider restDocumentationContextProvider) {

        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .apply(springSecurity())
                .apply(documentationConfiguration(restDocumentationContextProvider)
                        .operationPreprocessors()
                        .withRequestDefaults(modifyUris().host("localhost").removePort(), prettyPrint())
                        .withResponseDefaults(modifyUris().host("localhost").removePort(), prettyPrint()))
                .alwaysDo(print())
                .build();

        Member member = Member.builder()
                .nickname(USERNAME)
                .password(PASSWORD)
                .email(USERNAME)
                .image("test-image")
                .location("지역")
                .userRole(ROLE_MEMBER)
                .build();
        memberRepository.save(member);
    }

    @AfterEach
    public void after() {
        petRepository.deleteAllInBatch();
        refreshTokenRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }


    @Test
    @Transactional
    @DisplayName("반려동물 정보 작성 - 정상 응답")
    public void createPet() throws Exception {
        // Given
        PetRequestDto petRequestDto = PetRequestDto.builder()
                .name("멍멍이")
                .age(1)
                .weight(2.5)
                .species("강아지")
                .gender("여")
                .build();

        MockMultipartFile image = new MockMultipartFile(
                "image",
                "jjang.png",
                "image/png",
                "<<png data>>".getBytes());

        String petRequestDtoJson = objectMapper.writeValueAsString(petRequestDto);
        MockMultipartFile data = new MockMultipartFile(
                "data",
                "data",
                "application/json",
                petRequestDtoJson.getBytes(StandardCharsets.UTF_8));

        // When
        mockMvc.perform(multipart("/api/pet")
                        .file(data)
                        .file(image)
                        .header("Authorization", getAccessToken())
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(HAL_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("data.id").exists())
                .andDo(document("{class-name}/{method-name}",
                                requestHeaders(
                                        headerWithName(HttpHeaders.ACCEPT).description("accept header"),
                                        headerWithName(HttpHeaders.AUTHORIZATION).description("access token"),
                                        headerWithName(HttpHeaders.CONTENT_TYPE).description("content type")
                                ),
                                requestPartFields("data",
                                        fieldWithPath("name").description("name of petRequestDto"),
                                        fieldWithPath("age").description("age of petRequestDto"),
                                        fieldWithPath("weight").description("weight of petRequestDto"),
                                        fieldWithPath("species").description("species of petRequestDto"),
                                        fieldWithPath("gender").description("gender of petRequestDto")
                                ),
                                responseHeaders(
                                        headerWithName(HttpHeaders.CONTENT_TYPE).description("content type")
                                ),
                                responseFields(
                                        fieldWithPath("status").description("status of action"),
                                        fieldWithPath("message").description("message of action"),
                                        fieldWithPath("data.id").description("id of pet"),
                                        fieldWithPath("data.name").description("name of pet"),
                                        fieldWithPath("data.age").description("age of pet"),
                                        fieldWithPath("data.weight").description("weight of pet"),
                                        fieldWithPath("data.species").description("species of pet"),
                                        fieldWithPath("data.gender").description("gender of pet"),
                                        fieldWithPath("data.imageUrl").description("imageUrl of pet"),
                                        fieldWithPath("data.memberId").description("ud of member"),
                                        fieldWithPath("data.memberNickname").description("nickname of member"),
                                        fieldWithPath("data.createdAt").description("createdAt of pet"),
                                        fieldWithPath("data.modifiedAt").description("modifiedAt of pet"),
                                        fieldWithPath("data.links[0].rel").description("relation"),
                                        fieldWithPath("data.links[0].href").description("url of action")
                                )
                        )
                )
        ;



        // Then
        assertThat(petRequestDto.getAge()).isEqualTo(1);
        assertThat(petRequestDto.getWeight()).isEqualTo(2.5);
        assertThat(petRequestDto.getSpecies()).isEqualTo("강아지");
        assertThat(petRequestDto.getGender()).isEqualTo("여");

    }

    @Test
    @DisplayName("반려동물 정보 작성 - data 값이 빈 값으로 들어 온 경우 error 발생 (valid 유효성 검사)")
    public void createPet_DataEmpty() throws Exception {
        // Given
        PetRequestDto petRequestDto = PetRequestDto.builder()
                .build();

        MockMultipartFile image = new MockMultipartFile(
                "image",
                "jjang.png",
                "image/png",
                "<<png data>>".getBytes());

        String petRequestDtoJson = objectMapper.writeValueAsString(petRequestDto);
        MockMultipartFile data = new MockMultipartFile(
                "data",
                "data",
                "application/json",
                petRequestDtoJson.getBytes(StandardCharsets.UTF_8));

        // When & Then
        mockMvc.perform(multipart("/api/pet")
                        .file(data)
                        .file(image)
                        .header("Authorization", getAccessToken()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }


    @Test
    @Transactional
    @DisplayName("반려동물 조회 - 정상응답")
    public void getPet() throws Exception {
        // Given
        Member savedMember = memberRepository.findByNickname(USERNAME).orElseThrow();

        Pet pet = Pet.builder()
                .name("멍멍이")
                .age(2)
                .weight(3.5)
                .species("강아지")
                .gender("여")
                .imageUrl("imageUrl")
                .member(savedMember)
                .build();
        petRepository.save(pet);

        // When & Then
        this.mockMvc.perform(MockMvcRequestBuilders.get("/api/pet/" + pet.getId())
                        .header("Authorization", getAccessToken())
                        .contentType(APPLICATION_JSON)
                        .accept(HAL_JSON))
                .andExpect(status().isOk())
                .andDo(document("{class-name}/{method-name}",
                                requestHeaders(
                                        headerWithName(HttpHeaders.ACCEPT).description("accept header"),
                                        headerWithName(HttpHeaders.AUTHORIZATION).description("access token"),
                                        headerWithName(HttpHeaders.CONTENT_TYPE).description("content type")
                                ),
                                responseHeaders(
                                        headerWithName(HttpHeaders.CONTENT_TYPE).description("content type")
                                ),
                        responseFields(
                                fieldWithPath("status").description("status of action"),
                                fieldWithPath("message").description("message of action"),
                                fieldWithPath("data.id").description("id of pet"),
                                fieldWithPath("data.name").description("name of pet"),
                                fieldWithPath("data.age").description("age of pet"),
                                fieldWithPath("data.weight").description("weight of pet"),
                                fieldWithPath("data.species").description("species of pet"),
                                fieldWithPath("data.gender").description("gender of pet"),
                                fieldWithPath("data.imageUrl").description("imageUrl of pet"),
                                fieldWithPath("data.memberId").description("ud of member"),
                                fieldWithPath("data.memberNickname").description("nickname of member"),
                                fieldWithPath("data.createdAt").description("createdAt of pet"),
                                fieldWithPath("data.modifiedAt").description("modifiedAt of pet"),
                                fieldWithPath("data.links[0].rel").description("relation"),
                                fieldWithPath("data.links[0].href").description("url of action")
                        )
                        )
                )
        ;

        // Then
        assertThat(pet.getName()).isEqualTo("멍멍이");

    }

    @Test
    @DisplayName("반려동물 조회 - 반려동물이 존재하지 않는 경우 Error")
    public void getPet_Not_Exist() throws Exception {
        // Given
        Member savedMember = memberRepository.findByNickname(USERNAME).orElseThrow();

        Pet pet = Pet.builder()
                .name("멍멍이")
                .age(2)
                .weight(3.5)
                .species("강아지")
                .gender("여")
                .imageUrl("imageUrl")
                .member(savedMember)
                .build();
        petRepository.save(pet);

        // When
        this.mockMvc.perform(MockMvcRequestBuilders.get("/api/pet/" + pet.getId() + 1)
                        .header("Authorization", getAccessToken())
                        .contentType(APPLICATION_JSON)
                        .accept(HAL_JSON))
                .andExpect(status().is4xxClientError())
        ;
    }


    @Test
    @Transactional
    @DisplayName("반려동물 정보 수정 - 정상응답")
    public void putPet() throws Exception {
        // Given
        Member savedMember = memberRepository.findByNickname(USERNAME).orElseThrow();

        Pet pet = Pet.builder()
                .name("멍멍이")
                .age(2)
                .weight(3.5)
                .species("강아지")
                .gender("여")
                .imageUrl("imageUrl")
                .member(savedMember)
                .build();
        petRepository.save(pet);

        PetRequestDto petRequestDto = PetRequestDto.builder()
                .name("뭉뭉이")
                .age(3)
                .weight(4.5)
                .species("강아지")
                .gender("남")
                .build();

        MockMultipartFile image = new MockMultipartFile(
                "image",
                "memberImage.jpeg",
                "image/jpeg",
                "<<jpeg data>>".getBytes());

        String petRequestDtoJson = objectMapper.writeValueAsString(petRequestDto);
        MockMultipartFile data = new MockMultipartFile(
                "data",
                "data",
                "application/json",
                petRequestDtoJson.getBytes(StandardCharsets.UTF_8));

        // When & Then
        mockMvc.perform(multipartPutBuilder("/api/pet/" + pet.getId())
                        .file(data)
                        .file(image)
                        .header("Authorization", getAccessToken())
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(HAL_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("data.id").exists())
                .andDo(document("{class-name}/{method-name}",
                                requestHeaders(
                                        headerWithName(HttpHeaders.ACCEPT).description("accept header"),
                                        headerWithName(HttpHeaders.AUTHORIZATION).description("access token"),
                                        headerWithName(HttpHeaders.CONTENT_TYPE).description("content type")
                                ),
                        requestPartFields("data",
                                fieldWithPath("name").description("name of petRequestDto"),
                                fieldWithPath("age").description("age of petRequestDto"),
                                fieldWithPath("weight").description("weight of petRequestDto"),
                                fieldWithPath("species").description("species of petRequestDto"),
                                fieldWithPath("gender").description("gender of petRequestDto")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type")
                        ),
                        responseFields(
                                fieldWithPath("status").description("status of action"),
                                fieldWithPath("message").description("message of action"),
                                fieldWithPath("data.id").description("id of pet"),
                                fieldWithPath("data.name").description("name of pet"),
                                fieldWithPath("data.age").description("age of pet"),
                                fieldWithPath("data.weight").description("weight of pet"),
                                fieldWithPath("data.species").description("species of pet"),
                                fieldWithPath("data.gender").description("gender of pet"),
                                fieldWithPath("data.imageUrl").description("imageUrl of pet"),
                                fieldWithPath("data.memberId").description("ud of member"),
                                fieldWithPath("data.memberNickname").description("nickname of member"),
                                fieldWithPath("data.createdAt").description("createdAt of pet"),
                                fieldWithPath("data.modifiedAt").description("modifiedAt of pet"),
                                fieldWithPath("data.links[0].rel").description("relation"),
                                fieldWithPath("data.links[0].href").description("url of action")
                                )
                        )
                )
        ;

        // Then
        Pet savedPet = petRepository.findById(pet.getId()).orElseThrow();

        assertThat(savedPet.getName()).isEqualTo("뭉뭉이");
        assertThat(savedPet.getAge()).isEqualTo(3);
        assertThat(savedPet.getWeight()).isEqualTo(4.5);
        assertThat(savedPet.getSpecies()).isEqualTo("강아지");
        assertThat(savedPet.getGender()).isEqualTo("남");
    }

    @Test
    @Transactional
    @DisplayName("반려동물 정보 수정 - 정상응답(이미지는 수정하지 않는 경우)")
    public void putPet_NoImage() throws Exception {
        // Given
        Member savedMember = memberRepository.findByNickname(USERNAME).orElseThrow();

        Pet pet = Pet.builder()
                .name("멍멍이")
                .age(2)
                .weight(3.5)
                .species("강아지")
                .gender("여")
                .imageUrl("imageUrl")
                .member(savedMember)
                .build();
        petRepository.save(pet);

        PetRequestDto petRequestDto = PetRequestDto.builder()
                .name("뭉뭉이")
                .age(3)
                .weight(4.5)
                .species("강아지")
                .gender("남")
                .build();

        String petRequestDtoJson = objectMapper.writeValueAsString(petRequestDto);
        MockMultipartFile data = new MockMultipartFile(
                "data",
                "data",
                "application/json",
                petRequestDtoJson.getBytes(StandardCharsets.UTF_8));

        // When & Then
        mockMvc.perform(multipartPutBuilder("/api/pet/" + pet.getId())
                        .file(data)
                        .header("Authorization", getAccessToken())
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(HAL_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("data.id").exists())
                .andDo(document("{class-name}/{method-name}",
                                requestHeaders(
                                        headerWithName(HttpHeaders.ACCEPT).description("accept header"),
                                        headerWithName(HttpHeaders.AUTHORIZATION).description("access token"),
                                        headerWithName(HttpHeaders.CONTENT_TYPE).description("content type")
                                ),
                                requestPartFields("data",
                                        fieldWithPath("name").description("name of petRequestDto"),
                                        fieldWithPath("age").description("age of petRequestDto"),
                                        fieldWithPath("weight").description("weight of petRequestDto"),
                                        fieldWithPath("species").description("species of petRequestDto"),
                                        fieldWithPath("gender").description("gender of petRequestDto")
                                ),
                                responseHeaders(
                                        headerWithName(HttpHeaders.CONTENT_TYPE).description("content type")
                                ),
                                responseFields(
                                        fieldWithPath("status").description("status of action"),
                                        fieldWithPath("message").description("message of action"),
                                        fieldWithPath("data.id").description("id of pet"),
                                        fieldWithPath("data.name").description("name of pet"),
                                        fieldWithPath("data.age").description("age of pet"),
                                        fieldWithPath("data.weight").description("weight of pet"),
                                        fieldWithPath("data.species").description("species of pet"),
                                        fieldWithPath("data.gender").description("gender of pet"),
                                        fieldWithPath("data.imageUrl").description("imageUrl of pet"),
                                        fieldWithPath("data.memberId").description("ud of member"),
                                        fieldWithPath("data.memberNickname").description("nickname of member"),
                                        fieldWithPath("data.createdAt").description("createdAt of pet"),
                                        fieldWithPath("data.modifiedAt").description("modifiedAt of pet"),
                                        fieldWithPath("data.links[0].rel").description("relation"),
                                        fieldWithPath("data.links[0].href").description("url of action")
                                )
                        )
                )
        ;

        // Then
        Pet savedPet = petRepository.findById(pet.getId()).orElseThrow();

        assertThat(savedPet.getName()).isEqualTo("뭉뭉이");
        assertThat(savedPet.getAge()).isEqualTo(3);
        assertThat(savedPet.getWeight()).isEqualTo(4.5);
        assertThat(savedPet.getSpecies()).isEqualTo("강아지");
        assertThat(savedPet.getGender()).isEqualTo("남");
    }


    @Test
    @DisplayName("반려동물 정보 수정 - 권한이 없는 경우 Error")
    public void putPet_Not_Authorization() throws Exception {
        // Given
        Member member2 = Member.builder()
                .nickname("notAuthorization")
                .password(PASSWORD)
                .email("notAuthorization")
                .image("test-image")
                .userRole(ROLE_MEMBER)
                .build();
        memberRepository.save(member2);

        Pet pet = Pet.builder()
                .name("멍멍이")
                .age(2)
                .weight(3.5)
                .species("강아지")
                .gender("여")
                .imageUrl("imageUrl")
                .member(member2)
                .build();
        petRepository.save(pet);

        PetRequestDto petRequestDto = PetRequestDto.builder()
                .name("뭉뭉이")
                .age(3)
                .weight(4.5)
                .species("강아지")
                .gender("남")
                .build();

        String fileName = "memberImage";
        String contentType = "jpeg";
        String filePath = "src/test/resources/testImage/" + fileName + "." + contentType;
        FileInputStream fileInputStream = new FileInputStream(filePath);

        MockMultipartFile image = new MockMultipartFile(
                "image",
                fileName + "." + contentType,
                contentType,
                fileInputStream);

        String petRequestDtoJson = objectMapper.writeValueAsString(petRequestDto);
        MockMultipartFile data = new MockMultipartFile(
                "data",
                "data",
                "application/json",
                petRequestDtoJson.getBytes(StandardCharsets.UTF_8));

        // When & Then
        mockMvc.perform(multipartPutBuilder("/api/pet/" + pet.getId())
                        .file(data)
                        .file(image)
                        .header("Authorization", getAccessToken())
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(HAL_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }


    @Test
    @DisplayName("반려동물 정보 삭제 - 정상응답")
    public void deletePet() throws Exception {
        // Given
        Member savedMember = memberRepository.findByNickname(USERNAME).orElseThrow();

        Pet pet = Pet.builder()
                .name("멍멍이")
                .age(2)
                .weight(3.5)
                .species("강아지")
                .gender("여")
                .imageUrl("imageUrl")
                .member(savedMember)
                .build();
        petRepository.save(pet);

        // When
        this.mockMvc.perform(MockMvcRequestBuilders.delete("/api/pet/" + pet.getId())
                        .header("Authorization", getAccessToken())
                        .contentType(APPLICATION_JSON)
                        .accept(HAL_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("{class-name}/{method-name}",
                                requestHeaders(
                                        headerWithName(HttpHeaders.ACCEPT).description("accept header"),
                                        headerWithName(HttpHeaders.AUTHORIZATION).description("access token"),
                                        headerWithName(HttpHeaders.CONTENT_TYPE).description("content type")
                                ),
                                responseHeaders(
                                        headerWithName(HttpHeaders.CONTENT_TYPE).description("content type")
                                ),
                                responseFields(
                                        fieldWithPath("status").description("status of action"),
                                        fieldWithPath("message").description("message of action"),
                                        fieldWithPath("data.links[0].rel").description("relation"),
                                        fieldWithPath("data.links[0].href").description("url of action")
                                )
                        )
                )
        ;
        // Then
        assertThat(petRepository.findById(pet.getId())).isEmpty();

    }

    @Test
    @DisplayName("반려동물 정보 삭제 - 권한이 없는 경우 Error")
    public void deletePet_Not_Authorization() throws Exception {
        // Given
        Member member2 = Member.builder()
                .nickname("notAuthorization")
                .password(PASSWORD)
                .email("notAuthorization")
                .image("test-image")
                .userRole(ROLE_MEMBER)
                .build();
        memberRepository.save(member2);

        Pet pet = Pet.builder()
                .name("멍멍이")
                .age(2)
                .weight(3.5)
                .species("강아지")
                .gender("여")
                .imageUrl("imageUrl")
                .member(member2)
                .build();
        petRepository.save(pet);

        // When
        this.mockMvc.perform(MockMvcRequestBuilders.delete("/api/pet/" + pet.getId())
                        .header("Authorization", getAccessToken())
                        .contentType(APPLICATION_JSON)
                        .accept(HAL_JSON))
                .andExpect(status().is4xxClientError())
                .andDo(print());
    }








    private String getAccessToken() throws Exception {
        // Given
        LoginRequestDto loginRequestDto = new LoginRequestDto(USERNAME, PASSWORD);

        // When & Then
        ResultActions perform = this.mockMvc.perform(post("/api/member/login")
                        .contentType(APPLICATION_JSON)
                        .accept(HAL_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDto)))
                .andDo(print())
                .andExpect(status().isOk());

        return perform.andReturn().getResponse().getHeader("Authorization");
    }

    private MockMultipartHttpServletRequestBuilder multipartPutBuilder(final String url) {
        final MockMultipartHttpServletRequestBuilder builder = MockMvcRequestBuilders.multipart(url);
        builder.with(request1 -> {
            request1.setMethod(HttpMethod.PUT.name());
            return request1;
        });
        return builder;
    }




}