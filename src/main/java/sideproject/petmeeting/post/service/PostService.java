package sideproject.petmeeting.post.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import sideproject.petmeeting.common.S3Uploader;
import sideproject.petmeeting.common.exception.BusinessException;
import sideproject.petmeeting.common.exception.ErrorCode;
import sideproject.petmeeting.member.domain.Member;
import sideproject.petmeeting.post.domain.Category;
import sideproject.petmeeting.post.domain.HeartPost;
import sideproject.petmeeting.post.domain.Post;
import sideproject.petmeeting.post.dto.PostPageResponseDto;
import sideproject.petmeeting.post.dto.PostRequestDto;
import sideproject.petmeeting.post.dto.PostResponseDto;
import sideproject.petmeeting.post.repository.HeartPostRepository;
import sideproject.petmeeting.post.repository.PostRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class PostService {
    private final PostRepository postRepository;
    private final HeartPostRepository heartPostRepository;
    private final S3Uploader s3Uploader;

    /**
     * 게시글 작성
     * @param postRequestDto : 게시글 작성에 필요한 데이터
     * @param image          : 게시글에 첨부 할 이미지
     * @param member         : 작성자
     * @return : 응답 데이터 postResponseDto
     * @throws IOException : IOException 예외처리
     */
    @Transactional
    public PostResponseDto createPost(PostRequestDto postRequestDto, MultipartFile image, Member member) throws IOException {
        String imageUrl = s3Uploader.upload(image, "post/image");

        Post post = Post.builder()
                .category(postRequestDto.getCategory())
                .title(postRequestDto.getTitle())
                .content(postRequestDto.getContent())
                .imageUrl(imageUrl)
                .member(member)
                .build();
        postRepository.save(post);

        return getPostResponseDto(post);
    }


    /**
     * 게시글 전체 조회
     * @param pageNum : 조회할 페이지 번호
     * @return : 해당 페이지 번호의 전체 게시글, 페이지 정보
     */
    @Transactional(readOnly = true)
    public PostPageResponseDto getAllPosts(int pageNum) {
        Pageable pageable = PageRequest.of(pageNum, 15, Sort.by("modifiedAt").descending());

        Page<Post> postPage = postRepository.findAllByOrderByModifiedAtDesc(pageable);

        return getPostPageResponseDto(pageNum, postPage);

    }


    /**
     * 게시글 단건 조회
     * @param postId : 조회할 게시글 id
     * @return : 조회할 게시글
     */
    @Transactional
    public PostResponseDto getPost(Long postId) {
        Post post = postRepository.findPostFetchJoin(postId).orElseThrow(
                () -> new BusinessException("존재하지 않는 게시글 id 입니다.", ErrorCode.POST_NOT_EXIST)
        );

        post.viewCnt();
        return getPostResponseDto(post);
    }


    /**
     * 게시글 수정
     * @param postId         : 수정할 게시글 id
     * @param postRequestDto : 수정할 게시글
     * @param image          : 수정할 이미지 파일
     * @return : 수정 완료 게시글
     * @throws IOException : IOException 예외 처리
     */
    @Transactional
    public PostResponseDto updatePost(Long postId, PostRequestDto postRequestDto, MultipartFile image, Member member) throws IOException {
        Post post = postRepository.findById(postId).orElseThrow(
                () -> new BusinessException("존재하지 않는 게시글 id 입니다.", ErrorCode.POST_NOT_EXIST)
        );

        if (!post.getMember().getId().equals(member.getId())) {
            throw new BusinessException("수정 권한이 없습니다.", ErrorCode.HANDLE_ACCESS_DENIED);
        }

        String imageUrl = post.getImageUrl();

        // 이미지 존재 시 삭제 후 업로드
        if (imageUrl != null) {
            s3Uploader.deleteImage(imageUrl, "post/image");
        }

        imageUrl = s3Uploader.upload(image, "post/image");
        post.update(postRequestDto, imageUrl);

        return getPostResponseDto(post);
    }


    /**
     * 게시글 삭제
     *
     * @param postId : 삭제할 게시글 id
     * @throws IOException : 삭제할 게시글의 image 파일명 인코딩 예외 처리, UnsupportedEncodingException
     */
    @Transactional
    public void postDelete(Long postId, Member member) throws IOException {
        Post post = postRepository.findById(postId).orElseThrow(
                () -> new BusinessException("존재하지 않는 게시글 id 입니다.", ErrorCode.POST_NOT_EXIST)
        );

        if (!post.getMember().getId().equals(member.getId())) {
            throw new BusinessException("삭제 권한이 없습니다.", ErrorCode.HANDLE_ACCESS_DENIED);
        }

        String imageUrl = post.getImageUrl();

        if (imageUrl != null) {
            s3Uploader.deleteImage(imageUrl, "post/image");
        }

        postRepository.deleteById(postId);
    }

    /**
     * 게시글 좋아요
     * @param postId : '좋아요' 할 게시글 id
     * @param member : 게시글에 '좋아요'를 한 사용자
     */
    @Transactional
    public void addPostHeart(Long postId, Member member) {
        Post post = postRepository.findById(postId).orElseThrow(
                () -> new BusinessException("존재하지 않는 게시글 id 입니다.", ErrorCode.POST_NOT_EXIST)
        );

        if (heartPostRepository.findByPostAndMember(post, member).isPresent()) {
            throw new BusinessException("이미 '좋아요'한 게시글입니다.", ErrorCode.ALREADY_HEARTED);
        }

        HeartPost heartPost = HeartPost.builder()
                .post(post)
                .member(member)
                .build();

        heartPostRepository.save(heartPost);

        Integer countHeart = heartPostRepository.countByPostId(postId);

        post.addCountHeart(countHeart);
    }


    /**
     * 게시글 좋아요 취소
     * @param postId : '좋아요' 취소 할 게시글 id
     * @param member : 게시글에 '좋아요' 취소를 한 사용자
     */
    @Transactional
    public void deletePostHeart(Long postId, Member member) {
        Post post = postRepository.findById(postId).orElseThrow(
                () -> new BusinessException("존재하지 않는 게시글 id 입니다.", ErrorCode.POST_NOT_EXIST)
        );

        Optional<HeartPost> heartOptional = heartPostRepository.findByPostAndMember(post, member);
        if (heartOptional.isEmpty()) {
            throw new BusinessException("'좋아요' 하지 않은 게시글입니다.", ErrorCode.HEART_NOT_FOUND);
        }

        heartPostRepository.delete(heartOptional.get());

        Integer countHeart = heartPostRepository.countByPostId(postId);

        post.addCountHeart(countHeart);
    }


    /**
     * 게시글 검색
     * @param keyword: 검색 키워드
     * @return 검색 결과 응답
     */
    @Transactional(readOnly = true)
    public PostPageResponseDto searchPost(String keyword, int pageNum) {
        Pageable pageable = PageRequest.of(pageNum, 15, Sort.by("modifiedAt").descending());

        Page<Post> postPage = postRepository.findByKeyword(keyword, pageable);

        if(!postPage.hasContent()) {
            throw new BusinessException("검색 결과가 없습니다.", ErrorCode.KEYWORD_NOT_FOUND);
        }

        return getPostPageResponseDto(pageNum, postPage);
    }

    /**
     * 카테고리별 조회
     * @param category: 조회할 카테고리
     * @param pageNum: 조회할 페이지 번호
     * @return : 조회 결과 응답
     */
    @Transactional(readOnly = true)
    public PostPageResponseDto getCategoryPost(String category, int pageNum) {
        Pageable pageable = PageRequest.of(pageNum, 15, Sort.by("modifiedAt").descending());

        Category findCategory = Category.valueOf(category.toUpperCase());

        Page<Post> postPage = postRepository.findByCategory(findCategory, pageable);

        if(!postPage.hasContent()) {
            throw new BusinessException("해당 카테고리의 게시글이 없습니다.", ErrorCode.CATEGORY_NOT_FOUND);
        }

        return getPostPageResponseDto(pageNum, postPage);
    }




    /**
     * post 데이터를 postResponseDto 로 build
     * @param post : post 데이터
     * @return : 응답 데이터 postResponseDto
     */
    private PostResponseDto getPostResponseDto(Post post) {

        return PostResponseDto.builder()
                .id(post.getId())
                .category(post.getCategory())
                .title(post.getTitle())
                .content(post.getContent())
                .imageUrl(post.getImageUrl())
                .numHeart(post.getNumHeart())
                .viewCnt(post.getViewCnt())
                .authorId(post.getMember().getId())
                .authorNickname(post.getMember().getNickname())
                .authorLocation(post.getMember().getLocation())
                .authorImageUrl(post.getMember().getImage())
                .createdAt(post.getCreatedAt())
                .modifiedAt(post.getModifiedAt())
                .build();
    }


    private PostPageResponseDto getPostPageResponseDto(int pageNum, Page<Post> postPage) {
        List<Post> content = postPage.getContent();

        List<PostResponseDto> PostResponseDtoList = new ArrayList<>();
        for (Post post : content) {
            PostResponseDtoList.add(getPostResponseDto(post));
        }

        return PostPageResponseDto.builder()
                .postList(PostResponseDtoList)
                .totalPage(postPage.getTotalPages() - 1)
                .totalPost(postPage.getTotalElements())
                .currentPage(pageNum)
                .isFirstPage(postPage.isFirst())
                .hasNextPage(postPage.hasNext())
                .hasPreviousPage(postPage.hasPrevious())
                .build();
    }
}