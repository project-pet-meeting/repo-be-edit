package sideproject.petmeeting.post.service;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import sideproject.petmeeting.member.domain.Member;
import sideproject.petmeeting.member.repository.MemberRepository;
import sideproject.petmeeting.post.domain.Post;
import sideproject.petmeeting.post.dto.PostPageResponseDto;
import sideproject.petmeeting.post.dto.PostRequestDto;
import sideproject.petmeeting.post.dto.PostResponseDto;
import sideproject.petmeeting.post.repository.PostRepository;

import java.io.FileInputStream;
import java.io.IOException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static sideproject.petmeeting.member.domain.UserRole.ROLE_MEMBER;
import static sideproject.petmeeting.post.domain.Category.RECOMMAND;

@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(value = MethodOrderer.OrderAnnotation.class)
class PostServiceTest {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    PostRepository postRepository;

    @Autowired
    PostService postService;
    public static final String USERNAME = "postService@Username.com";
    public static final String PASSWORD = "password";


    @Test
    @Order(0)
    @DisplayName("공통으로 사용하는 ENTITY 생성")
    public void entityBuild() {
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
    @DisplayName("게시글 작성 테스트 - 정상 응답")
    public void createPostTest() throws IOException {
        // Given
        PostRequestDto postRequestDto = PostRequestDto.builder()
                .category(RECOMMAND)
                .title("제목입니다.")
                .content("내용입니다.")
                .build();

        String fileName = "jjang";
        String contentType = "png";
        String filePath = "src/test/resources/testImage/" + fileName + "." + contentType;
        FileInputStream fileInputStream = new FileInputStream(filePath);

        MockMultipartFile image = new MockMultipartFile(
                "image",
                fileName + "." + contentType,
                contentType,
                fileInputStream);

        Member savedMember = memberRepository.findByNickname(USERNAME).orElseThrow();
        // When
        PostResponseDto savedPost = postService.createPost(postRequestDto, image, savedMember);

        // Then
        assertThat(savedPost.getCategory()).isEqualTo(RECOMMAND);
        assertThat(savedPost.getTitle()).isEqualTo("제목입니다.");
        assertThat(savedPost.getContent()).isEqualTo("내용입니다.");
    }

    @Test
    @DisplayName("게시글 전체 조회 테스트 - 정상 응답")
    public void getAllPostsTest() {
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
                .category(RECOMMAND)
                .title("second post title")
                .content("second post content")
                .member(savedMember)
                .imageUrl("imageUrl")
                .numHeart(0)
                .build();
        postRepository.save(secondPost);

        // When
        PostPageResponseDto savedPost = postService.getAllPosts(0);

        // Then
//        assertThat(savedPost).isEqualTo(2);

    }

    @Test
    @DisplayName("게시글 단일 조회 테스트 - 정상 응답")
    public void getPostTest() {
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
        PostResponseDto savedPost = postService.getPost(firstPost.getId());

        // Then
        assertThat(savedPost.getCategory()).isEqualTo(RECOMMAND);
        assertThat(savedPost.getTitle()).isEqualTo("first post title");
        assertThat(savedPost.getContent()).isEqualTo("first post content");
    }

    @Test
    @DisplayName("게시글 수정 테스트 - 정상 응답")
    public void updatePostTest() throws IOException {
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
                .category(RECOMMAND)
                .title("제목입니다.")
                .content("내용입니다.")
                .build();

        String fileName = "jjang";
        String contentType = "png";
        String filePath = "src/test/resources/testImage/" + fileName + "." + contentType;
        FileInputStream fileInputStream = new FileInputStream(filePath);

        MockMultipartFile image = new MockMultipartFile(
                "image",
                fileName + "." + contentType,
                contentType,
                fileInputStream);

        // When
        PostResponseDto savedPost = postService.updatePost(firstPost.getId(), postRequestDto, image, savedMember);

        // Then
        assertThat(savedPost.getCategory()).isEqualTo(RECOMMAND);
        assertThat(savedPost.getTitle()).isEqualTo("제목입니다.");
        assertThat(savedPost.getContent()).isEqualTo("내용입니다.");
    }

    @Test
    @DisplayName("게시글 삭제 테스트 - 정상 응답")
    public void deletePostTest() throws IOException {
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
        postService.postDelete(firstPost.getId(), savedMember);

        // Then

    }

}