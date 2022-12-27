package sideproject.petmeeting.comment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import sideproject.petmeeting.comment.domain.Comment;
import sideproject.petmeeting.comment.dto.request.CommentRequestDto;
import sideproject.petmeeting.comment.dto.request.CommentUpdateRequest;
import sideproject.petmeeting.comment.repository.CommentRepository;
import sideproject.petmeeting.member.domain.Member;
import sideproject.petmeeting.member.dto.request.LoginRequestDto;
import sideproject.petmeeting.member.repository.MemberRepository;
import sideproject.petmeeting.post.domain.Post;
import sideproject.petmeeting.post.repository.PostRepository;
import sideproject.petmeeting.token.repository.RefreshTokenRepository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
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


@Transactional
@ExtendWith({SpringExtension.class, RestDocumentationExtension.class})
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class CommentControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    RefreshTokenRepository refreshTokenRepository;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    PostRepository postRepository;
    @Autowired
    CommentRepository commentRepository;

    @BeforeEach
    public void setup(WebApplicationContext webApplicationContext,
                      RestDocumentationContextProvider restDocumentationContextProvider) {
        Member member = buildMember();
        memberRepository.save(member);

        Post post = buildPost();
        postRepository.save(post);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
//                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .apply(documentationConfiguration(restDocumentationContextProvider)
                        .operationPreprocessors()
                        .withRequestDefaults(modifyUris().host("tommy.me").removePort(), prettyPrint())
                        .withResponseDefaults(modifyUris().host("tommy.me").removePort(), prettyPrint()))
                .alwaysDo(print())
                .build();
    }

    private String getAccessToken() throws Exception {
        // Given
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
    @DisplayName("정상적인 요청이 들어왔을 때 Test")
    public void createComment() throws Exception {
        // Given
        CommentRequestDto commentRequestDto = CommentRequestDto.builder()
                .content("test comment")
                .build();

        Post savedPost = postRepository.findByTitle("post").get();

        // When

        this.mockMvc.perform(post("/api/comment/" + savedPost.getId())
                        .header("Authorization", getAccessToken())
                        .contentType(APPLICATION_JSON)
                        .accept(HAL_JSON)
                        .content(objectMapper.writeValueAsString(commentRequestDto)))
                .andExpect(status().isCreated())
                .andDo(document("create-comment",
                        requestHeaders(
                                headerWithName(HttpHeaders.ACCEPT).description("accept header"),
                                headerWithName(HttpHeaders.AUTHORIZATION).description("access token"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type")
                        ),
                        requestFields(
                                fieldWithPath("content").description("comment content")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type")
                        ),
                        responseFields(
                                fieldWithPath("status").description("status of action"),
                                fieldWithPath("message").description("message of action"),
                                fieldWithPath("data.object").description("id of created comment"),
                                fieldWithPath("data.links[0].rel").description("relation"),
                                fieldWithPath("data.links[0].href").description("url of action")
                        )
                ));

        Comment comment = commentRepository.findById(1L).get();
        // Then
        assertThat(commentRepository.findAll().size()).isEqualTo(1);
        assertThat(comment.getPost().getId()).isEqualTo(savedPost.getId());
        assertThat(comment.getMember().getNickname()).isEqualTo("Tommy");
    }

    @Test
    @DisplayName("게시글이 존재하지 않는 경우 에러 발생")
    public void createComment_No_Post() throws Exception {
        // Given
        CommentRequestDto commentRequestDto = CommentRequestDto.builder()
                .content("test comment")
                .build();

        this.mockMvc.perform(post("/api/comment/100")
                        .header("Authorization", getAccessToken())
                        .contentType(APPLICATION_JSON)
                        .accept(HAL_JSON)
                        .content(objectMapper.writeValueAsString(commentRequestDto)))
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @DisplayName("댓글에 내용이 담기지 않을 경우 에러 처리")
    public void createComment_No_Content() throws Exception {
        // Given
        CommentRequestDto commentRequestDto = CommentRequestDto.builder()
                .build();

        Post savedPost = postRepository.findByTitle("post").get();

        this.mockMvc.perform(post("/api/comment/" + savedPost.getId())
                        .header("Authorization", getAccessToken())
                        .contentType(APPLICATION_JSON)
                        .accept(HAL_JSON)
                        .content(objectMapper.writeValueAsString(commentRequestDto)))
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @DisplayName("정상적인 댓글 조회")
    public void getCommentList() throws Exception {
        // Given

        Member savedMember = memberRepository.findByEmail("test@test.com").get();
        Post savedPost = postRepository.findByTitle("post").get();

        Comment firstComment = Comment.builder()
                .content("first comment")
                .member(savedMember)
                .post(savedPost)
                .build();

        Comment secondComment = Comment.builder()
                .content("second comment")
                .member(savedMember)
                .post(savedPost)
                .build();
        commentRepository.save(firstComment);
        commentRepository.save(secondComment);

        this.mockMvc.perform(get("/api/comment/" + savedPost.getId())
                        .header("Authorization", getAccessToken())
                        .contentType(APPLICATION_JSON)
                        .accept(HAL_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("data.object[0].id").exists())
                .andExpect(jsonPath("data.object[0].content").exists())
                .andDo(document("get-comment",
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
                                        fieldWithPath("data.object[0].id").description("id of comment"),
                                        fieldWithPath("data.object[0].content").description("content of comment"),
                                        fieldWithPath("data.links[0].rel").description("relation"),
                                        fieldWithPath("data.links[0].href").description("url of action")
                                )
                        )

                )
        ;
    }

    @Test
    @DisplayName("게시글이 존재하지 않을 때 댓글 조회 시 에러 발생")
    public void getComment_No_Post() throws Exception {
        // Given

        this.mockMvc.perform(get("/api/comment/100")
                        .header("Authorization", getAccessToken())
                        .contentType(APPLICATION_JSON)
                        .accept(HAL_JSON))
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @DisplayName("정상적인 요청 시 댓글 업데이트")
    public void updateComment() throws Exception {
        Member savedMember = memberRepository.findByEmail("test@test.com").get();
        Post savedPost = postRepository.findByTitle("post").get();

        Comment comment = Comment.builder()
                .content("first comment")
                .member(savedMember)
                .post(savedPost)
                .build();
        Comment savedComment = commentRepository.save(comment);

        CommentUpdateRequest updatedComment = CommentUpdateRequest.builder()
                .content("updated comment")
                .build();

        this.mockMvc.perform(put("/api/comment/" + savedComment.getId())
                        .header("Authorization", getAccessToken())
                        .contentType(APPLICATION_JSON)
                        .accept(HAL_JSON)
                        .content(objectMapper.writeValueAsString(updatedComment)))
                .andExpect(status().isOk())
                .andDo(document("update-comment",
                        requestHeaders(
                                headerWithName(HttpHeaders.ACCEPT).description("accept header"),
                                headerWithName(HttpHeaders.AUTHORIZATION).description("access token"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type")
                        ),
                        requestFields(
                                fieldWithPath("content").description("update content")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type")
                        ),
                        responseFields(
                                fieldWithPath("status").description("status of action"),
                                fieldWithPath("message").description("message of action"),
                                fieldWithPath("data.object").description("id of updated comment"),
                                fieldWithPath("data.links[0].rel").description("relation"),
                                fieldWithPath("data.links[0].href").description("url of action")
                        )
                ))
        ;

        Comment findComment = commentRepository.findById(savedComment.getId()).get();
        assertThat(findComment.getContent()).isEqualTo("updated comment");
    }

    @Test
    @DisplayName("비정상적인 요청시 에러 발생")
    public void updateComment_BAD_REQUEST() throws Exception {
        Member savedMember = memberRepository.findByEmail("test@test.com").get();
        Post savedPost = postRepository.findByTitle("post").get();

        Comment comment = Comment.builder()
                .content("first comment")
                .member(savedMember)
                .post(savedPost)
                .build();
        commentRepository.save(comment);

        CommentUpdateRequest updatedComment = CommentUpdateRequest.builder()
                .content("updated comment")
                .build();

        this.mockMvc.perform(put("/api/comment/2")
                        .header("Authorization", getAccessToken())
                        .contentType(APPLICATION_JSON)
                        .accept(HAL_JSON)
                        .content(objectMapper.writeValueAsString(updatedComment)))
                .andExpect(status().isBadRequest())
        ;
    }

    @Test
    @DisplayName("정상적인 요청시 댓글 삭제")
    public void deleteComment() throws Exception {
        Member savedMember = memberRepository.findByEmail("test@test.com").get();
        Post savedPost = postRepository.findByTitle("post").get();

        Comment comment = Comment.builder()
                .content("first comment")
                .member(savedMember)
                .post(savedPost)
                .build();
        Comment savedComment = commentRepository.save(comment);

        this.mockMvc.perform(delete("/api/comment/" + savedComment.getId())
                        .header("Authorization", getAccessToken())
                        .contentType(APPLICATION_JSON)
                        .accept(HAL_JSON))
                .andExpect(status().isOk())
                .andDo(document("delete-comment",
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
                                        fieldWithPath("data.object").description("id of deleted comment"),
                                        fieldWithPath("data.links[0].rel").description("relation"),
                                        fieldWithPath("data.links[0].href").description("url of action")
                                )
                        )
                );
        ;
        assertThat(commentRepository.findById(savedComment.getId())).isEmpty();
    }

    @Test
    @DisplayName("비정상적인 요청시 오류 발생")
    public void deleteComment_BAD_REQUEST() throws Exception {
        Member savedMember = memberRepository.findByEmail("test@test.com").get();
        Post savedPost = postRepository.findByTitle("post").get();

        Comment comment = Comment.builder()
                .content("first comment")
                .member(savedMember)
                .post(savedPost)
                .build();
        commentRepository.save(comment);

        this.mockMvc.perform(delete("/api/comment/100")
                        .header("Authorization", getAccessToken())
                        .contentType(APPLICATION_JSON)
                        .accept(HAL_JSON))
                .andExpect(status().isBadRequest())
        ;
    }

    private static Member buildMember() {

        Member member = Member.builder()
                .id(1L)
                .nickname("Tommy")
                .password("test")
                .email("test@test.com")
                .image("test-image")
                .userRole(ROLE_MEMBER)
                .build();
        return member;
    }

    private Post buildPost() {
        Post post = Post.builder()
                .title("post")
                .content("content")
                .imageUrl("test-test.com")
                .member(memberRepository.findByEmail("test@test.com").get())
                .build();
        return post;
    }
}