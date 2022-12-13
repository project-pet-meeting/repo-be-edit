package sideproject.petmeeting.post.controller;

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
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import sideproject.petmeeting.member.domain.Member;
import sideproject.petmeeting.member.dto.request.LoginRequestDto;
import sideproject.petmeeting.member.repository.MemberRepository;
import sideproject.petmeeting.post.domain.Category;
import sideproject.petmeeting.post.domain.HeartPost;
import sideproject.petmeeting.post.domain.Post;
import sideproject.petmeeting.post.dto.PostRequestDto;
import sideproject.petmeeting.post.repository.HeartPostRepository;
import sideproject.petmeeting.post.repository.PostRepository;
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
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static sideproject.petmeeting.member.domain.UserRole.ROLE_MEMBER;
import static sideproject.petmeeting.post.domain.Category.FREEPRESENT;
import static sideproject.petmeeting.post.domain.Category.RECOMMAND;

@ExtendWith({SpringExtension.class, RestDocumentationExtension.class})
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@TestMethodOrder(value = MethodOrderer.OrderAnnotation.class)
@Slf4j
class PostControllerTest {
    @Autowired
    private HeartPostRepository heartPostRepository;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    PostRepository postRepository;
    @Autowired
    RefreshTokenRepository refreshTokenRepository;

    public static final String USERNAME = "postController@Username.com";
    public static final String PASSWORD = "password";


    @BeforeEach
    public void setup(RestDocumentationContextProvider restDocumentationContextProvider) {

        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .apply(springSecurity())
                .apply(documentationConfiguration(restDocumentationContextProvider)
                        .operationPreprocessors()
                        .withRequestDefaults(modifyUris().host("tommy.me").removePort(), prettyPrint())
                        .withResponseDefaults(modifyUris().host("tommy.me").removePort(), prettyPrint()))
                .alwaysDo(print())
                .build();
    }

    @Order(0)
    @Test
    @DisplayName("공통으로 사용하는 ENTITY 생성")
    public void memberBuild() {
        Member member = Member.builder()
                .nickname(USERNAME)
                .password(PASSWORD)
                .email(USERNAME)
                .image("test-image")
                .userRole(ROLE_MEMBER)
                .build();
        memberRepository.save(member);
    }

    @Test
    @DisplayName("게시글 작성 - 정상 응답")
    public void createPost() throws Exception {
        // Given
        PostRequestDto postRequestDto = PostRequestDto.builder()
                .category(Category.valueOf("RECOMMAND"))
                .title("제목입니다.")
                .content("내용입니다.")
                .build();

        // MockMultipartFile 을  MultipartFile 인터페이스를 상속받아 mock 구현
        String fileName = "jjang";
        String contentType = "png";
        String filePath = "src/test/resources/testImage/" + fileName + "." + contentType;
        FileInputStream fileInputStream = new FileInputStream(filePath);

        MockMultipartFile image = new MockMultipartFile(
                "image",
                fileName + "." + contentType,
                contentType,
                fileInputStream);

        String postRequestDtoJson = objectMapper.writeValueAsString(postRequestDto);
        MockMultipartFile data = new MockMultipartFile(
                "data",
                "data",
                "application/json",
                postRequestDtoJson.getBytes(StandardCharsets.UTF_8));

        // When
        mockMvc.perform(multipart("/api/post")
                                .file(data)
                                .file(image)
                                .header("Authorization", getAccessToken())
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(HAL_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("data.id").exists())
//                .andDo(document("{method-name}-post",
//                                requestHeaders(
//                                        headerWithName(HttpHeaders.ACCEPT).description("accept header"),
//                                        headerWithName(HttpHeaders.AUTHORIZATION).description("access token"),
//                                        headerWithName(HttpHeaders.CONTENT_TYPE).description("content type")
//                                ),
//                                responseHeaders(
//                                        headerWithName(HttpHeaders.CONTENT_TYPE).description("content type")
//                                ),
//                                responseFields(
//                                        fieldWithPath("status").description("status of action"),
//                                        fieldWithPath("message").description("message of action"),
//                                        fieldWithPath("data.id").description("id of post"),
//                                        fieldWithPath("data.category").description("category of post"),
//                                        fieldWithPath("data.title").description("title of post"),
//                                        fieldWithPath("data.content").description("content of post"),
//                                        fieldWithPath("data.imageUrl").description("imageUrl of post"),
//                                        fieldWithPath("data.numHeart").description("numHeart of post"),
//                                        fieldWithPath("data.authorId").description("authorId of post"),
//                                        fieldWithPath("data.authorNickname").description("authorNickname of post"),
//                                        fieldWithPath("data.authorImageUrl").description("authorImageUrl of post"),
//                                        fieldWithPath("data.createdAt").description("createdAt of post"),
//                                        fieldWithPath("data..modifiedAt").description("modifiedAt of post"),
//                                        fieldWithPath("data.links[0].rel").description("relation"),
//                                        fieldWithPath("data.links[0].href").description("url of action")
//                                )
//                        )
//                )
        ;

        // Then
        assertThat(postRequestDto.getCategory()).isEqualTo(RECOMMAND);
        assertThat(postRequestDto.getTitle()).isEqualTo("제목입니다.");
        assertThat(postRequestDto.getContent()).isEqualTo("내용입니다.");
    }

    @Test
    @DisplayName("게시글 작성 - data 값이 빈값으로 들어 온 경우 error 발생 (valid 유효성 검사)")
    public void createPost_DataEmpty() throws Exception {

        // Given
        PostRequestDto postRequestDto = PostRequestDto.builder()
                .build();

        // MockMultipartFile 을  MultipartFile 인터페이스를 상속받아 mock 구현
        String fileName = "jjang";
        String contentType = "png";
        String filePath = "src/test/resources/testImage/" + fileName + "." + contentType;
        FileInputStream fileInputStream = new FileInputStream(filePath);

        MockMultipartFile image = new MockMultipartFile(
                "image",
                fileName + "." + contentType,
                contentType,
                fileInputStream);

        String postRequestDtoJson = objectMapper.writeValueAsString(postRequestDto);
        MockMultipartFile data = new MockMultipartFile(
                "data",
                "data",
                "application/json",
                postRequestDtoJson.getBytes(StandardCharsets.UTF_8));

        // When & Then
        mockMvc.perform(multipart("/api/post")
                        .file(data)
                        .file(image)
                        .header("Authorization", getAccessToken()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("전체 게시글 조회 - 정상응답")
    public void getAllPosts() throws Exception {
        // Given
        Member savedMember = memberRepository.findByNickname(USERNAME).orElseThrow();

        Post firstPost = Post.builder()
                .category(RECOMMAND)
                .title("first post title")
                .content("first post content")
                .member(savedMember)
                .imageUrl("imageUrl")
                .numHeart(0)
                .build();
        postRepository.save(firstPost);

        Post secondPost = Post.builder()
                .category(FREEPRESENT)
                .title("second post title")
                .content("second post content")
                .member(savedMember)
                .imageUrl("imageUrl")
                .numHeart(0)
                .build();
        postRepository.save(secondPost);

        // When & Then
        this.mockMvc.perform(MockMvcRequestBuilders.get("/api/post")
                .param("page", String.valueOf(0))
                .header("Authorization", getAccessToken())
                .contentType(APPLICATION_JSON)
                .accept(HAL_JSON))
                .andExpect(status().isOk())
                .andDo(document("get-allPosts",
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
                                        fieldWithPath("data.postList[].id").description("id of post"),
                                        fieldWithPath("data.postList[].category").description("category of post"),
                                        fieldWithPath("data.postList[].title").description("title of post"),
                                        fieldWithPath("data.postList[].content").description("content of post"),
                                        fieldWithPath("data.postList[].imageUrl").description("imageUrl of post"),
                                        fieldWithPath("data.postList[].numHeart").description("numHeart of post"),
                                        fieldWithPath("data.postList[].authorId").description("authorId of post"),
                                        fieldWithPath("data.postList[].authorNickname").description("authorNickname of post"),
                                        fieldWithPath("data.postList[].authorImageUrl").description("authorImageUrl of post"),
                                        fieldWithPath("data.postList[].createdAt").description("createdAt of post"),
                                        fieldWithPath("data.postList[].modifiedAt").description("modifiedAt of post"),
                                        fieldWithPath("data.totalPage").description("totalPage of postList"),
                                        fieldWithPath("data.currentPage").description("currentPage of postList"),
                                        fieldWithPath("data.totalPost").description("totalPost of postList"),
                                        fieldWithPath("data.hasNextPage").description("hasNextPage of postList"),
                                        fieldWithPath("data.hasPreviousPage").description("hasPreviousPage of postList"),
                                        fieldWithPath("data.firstPage").description("firstPage of postList"),
                                        fieldWithPath("data.links[0].rel").description("relation"),
                                        fieldWithPath("data.links[0].href").description("url of action")
                                )
                        )
                )
        ;

    }


    @Test
    @DisplayName("단일 게시글 조회 - 정상응답")
    public void getPost() throws Exception {
        // Given
        Member savedMember = memberRepository.findByNickname(USERNAME).orElseThrow();

        Post firstPost = Post.builder()
                .category(RECOMMAND)
                .title("first post title")
                .content("first post content")
                .member(savedMember)
                .imageUrl("imageUrl")
                .numHeart(0)
                .build();
        postRepository.save(firstPost);

        // When & Then
        this.mockMvc.perform(MockMvcRequestBuilders.get("/api/post/" + firstPost.getId())
                        .header("Authorization", getAccessToken())
                        .contentType(APPLICATION_JSON)
                        .accept(HAL_JSON))
                .andExpect(status().isOk())
                .andDo(document("get-Post",
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
                                        fieldWithPath("data.id").description("id of post"),
                                        fieldWithPath("data.category").description("category of post"),
                                        fieldWithPath("data.title").description("title of post"),
                                        fieldWithPath("data.content").description("content of post"),
                                        fieldWithPath("data.imageUrl").description("imageUrl of post"),
                                        fieldWithPath("data.numHeart").description("numHeart of post"),
                                        fieldWithPath("data.authorId").description("authorId of post"),
                                        fieldWithPath("data.authorNickname").description("authorNickname of post"),
                                        fieldWithPath("data.authorImageUrl").description("authorImageUrl of post"),
                                        fieldWithPath("data.createdAt").description("createdAt of post"),
                                        fieldWithPath("data..modifiedAt").description("modifiedAt of post"),
                                        fieldWithPath("data.links[0].rel").description("relation"),
                                        fieldWithPath("data.links[0].href").description("url of action")
                                )
                        )
                )
        ;

        // Then
        assertThat(firstPost.getCategory()).isEqualTo(RECOMMAND);
        assertThat(firstPost.getTitle()).isEqualTo("first post title");
        assertThat(firstPost.getContent()).isEqualTo("first post content");
        assertThat(firstPost.getMember().getNickname()).isEqualTo(USERNAME);
    }

    @Test
    @DisplayName("게시글 조회 - 게시글이 존재하지 않는 경우 Error")
    public void getPost_No_Post() throws Exception {
        // Given
        Member savedMember = memberRepository.findByNickname(USERNAME).orElseThrow();

        Post firstPost = Post.builder()
                .category(RECOMMAND)
                .title("first post title")
                .content("first post content")
                .member(savedMember)
                .imageUrl("imageUrl")
                .numHeart(0)
                .build();
        postRepository.save(firstPost);

        // When
        this.mockMvc.perform(MockMvcRequestBuilders.get("/api/post/" + firstPost.getId() + 1)
                        .header("Authorization", getAccessToken())
                        .contentType(APPLICATION_JSON)
                        .accept(HAL_JSON))
                .andExpect(status().is4xxClientError())
        ;
    }

    @Test
    @DisplayName("게시글 수정 - 정상응답")
    public void putPost() throws Exception {
        // Given
        Member savedMember = memberRepository.findByNickname(USERNAME).orElseThrow();

        Post firstPost = Post.builder()
                .category(RECOMMAND)
                .title("first post title")
                .content("first post content")
                .member(savedMember)
                .imageUrl("imageUrl")
                .numHeart(0)
                .build();
        postRepository.save(firstPost);

        PostRequestDto postRequestDto = PostRequestDto.builder()
                .category(FREEPRESENT)
                .title("수정 제목")
                .content("수정 내용")
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

        String postRequestDtoJson = objectMapper.writeValueAsString(postRequestDto);
        MockMultipartFile data = new MockMultipartFile(
                "data",
                "data",
                "application/json",
                postRequestDtoJson.getBytes(StandardCharsets.UTF_8));

        // When & Then
        mockMvc.perform(multipartPutBuilder("/api/post/" + firstPost.getId())
                        .file(data)
                        .file(image)
                        .header("Authorization", getAccessToken())
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(HAL_JSON)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("data.id").exists());

        // Then
        assertThat(postRequestDto.getCategory()).isEqualTo(FREEPRESENT);
        assertThat(postRequestDto.getTitle()).isEqualTo("수정 제목");
        assertThat(postRequestDto.getContent()).isEqualTo("수정 내용");
    }

    @Test
    @DisplayName("게시글 수정 - 권한이 없는 경우 Error")
    public void putPost_Not_Authorization() throws Exception {
        // Given
        Member member2 = Member.builder()
                .nickname("notAuthorization")
                .password(PASSWORD)
                .email("notAuthorization")
                .image("test-image")
                .userRole(ROLE_MEMBER)
                .build();
        memberRepository.save(member2);

        Post firstPost = Post.builder()
                .category(RECOMMAND)
                .title("first post title")
                .content("first post content")
                .member(memberRepository.findByNickname("notAuthorization").orElseThrow())
                .imageUrl("imageUrl")
                .numHeart(0)
                .build();
        postRepository.save(firstPost);

        PostRequestDto postRequestDto = PostRequestDto.builder()
                .category(FREEPRESENT)
                .title("수정 제목")
                .content("수정 내용")
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

        String postRequestDtoJson = objectMapper.writeValueAsString(postRequestDto);
        MockMultipartFile data = new MockMultipartFile(
                "data",
                "data",
                "application/json",
                postRequestDtoJson.getBytes(StandardCharsets.UTF_8));

        // When & Then
        mockMvc.perform(multipartPutBuilder("/api/post/" + firstPost.getId())
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
    @DisplayName("게시글 삭제 - 정상응답")
    public void deletePost() throws Exception {
        // Given
        Member savedMember = memberRepository.findByNickname(USERNAME).orElseThrow();

        Post firstPost = Post.builder()
                .category(RECOMMAND)
                .title("first post title")
                .content("first post content")
                .member(savedMember)
                .imageUrl("imageUrl")
                .numHeart(0)
                .build();
        postRepository.save(firstPost);

        // When
        this.mockMvc.perform(MockMvcRequestBuilders.delete("/api/post/" + firstPost.getId())
                        .param("postId", firstPost.getId().toString())
                        .header("Authorization", getAccessToken())
                        .contentType(APPLICATION_JSON)
                        .accept(HAL_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("delete-post",
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

    }

    @Test
    @DisplayName("게시글 삭제 - 권한이 없는 경우 Error")
    public void deletePost_Not_Authorization() throws Exception {
        // Given
        Member member2 = Member.builder()
                .nickname("notAuthorization2")
                .password(PASSWORD)
                .email("notAuthorization2")
                .image("test-image")
                .userRole(ROLE_MEMBER)
                .build();
        memberRepository.save(member2);

        Post firstPost = Post.builder()
                .category(RECOMMAND)
                .title("first post title")
                .content("first post content")
                .member(memberRepository.findByNickname("notAuthorization2").orElseThrow())
                .imageUrl("imageUrl")
                .numHeart(0)
                .build();
        postRepository.save(firstPost);

        // When
        this.mockMvc.perform(MockMvcRequestBuilders.delete("/api/post/" + firstPost.getId())
                        .param("postId", firstPost.getId().toString())
                        .header("Authorization", getAccessToken())
                        .contentType(APPLICATION_JSON)
                        .accept(HAL_JSON))
                .andExpect(status().is4xxClientError())
                .andDo(print());
    }

    @Test
    @DisplayName("게시글 좋아요 - 정상 응답")
    public void createHeartPost() throws Exception {
        // Given
        Member savedMember = memberRepository.findByNickname(USERNAME).orElseThrow();

        Post firstPost = Post.builder()
                .category(RECOMMAND)
                .title("first post title")
                .content("first post content")
                .member(savedMember)
                .imageUrl("imageUrl")
                .numHeart(0)
                .build();
        postRepository.save(firstPost);

        // When & Then
        this.mockMvc.perform(MockMvcRequestBuilders.post("/api/post/heart/" + firstPost.getId())
                        .header("Authorization", getAccessToken())
                        .contentType(APPLICATION_JSON)
                        .accept(HAL_JSON))
                .andExpect(status().isOk())
                .andDo(document("post-HeartPost",
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

    }

//    @Transactional
    @Test
    @DisplayName("게시글 좋아요 취소 - 정상응답")
    public void deleteHeartPost() throws Exception {
        // Given
        Member savedMember = memberRepository.findByNickname(USERNAME).orElseThrow();

        Post firstPost = Post.builder()
                .category(RECOMMAND)
                .title("first post title")
                .content("first post content")
                .member(savedMember)
                .imageUrl("imageUrl")
                .numHeart(0)
                .build();
        postRepository.save(firstPost);

        HeartPost heart = HeartPost.builder()
                .post(firstPost)
                .member(savedMember)
                .build();
        heartPostRepository.save(heart);

        // When & Then
        this.mockMvc.perform(MockMvcRequestBuilders.delete("/api/post/heart/" + firstPost.getId())
                        .header("Authorization", getAccessToken())
                        .contentType(APPLICATION_JSON)
                        .accept(HAL_JSON))
                .andExpect(status().isOk())
                .andDo(document("delete-HeartPost",
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
//        assertThat(refreshTokenRepository.findAll().size()).isEqualTo(1);

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