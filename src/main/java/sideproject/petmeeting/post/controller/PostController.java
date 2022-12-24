package sideproject.petmeeting.post.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sideproject.petmeeting.common.Response;
import sideproject.petmeeting.common.ResponseResource;
import sideproject.petmeeting.common.StatusEnum;
import sideproject.petmeeting.post.dto.PostPageResponseDto;
import sideproject.petmeeting.post.dto.PostRequestDto;
import sideproject.petmeeting.post.dto.PostResponseDto;
import sideproject.petmeeting.post.service.PostService;
import sideproject.petmeeting.security.UserDetailsImpl;

import javax.validation.Valid;
import java.io.IOException;

import static org.springframework.hateoas.MediaTypes.HAL_JSON_VALUE;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api", produces = HAL_JSON_VALUE)
public class PostController {
    private final PostService postService;

    /**
     * 게시글 작성
     * @param postRequestDto : 게시글 작성에 필요한 데이터
     * @param image : 게시글에 첨부 할 이미지
     * @return :
     */
    @PostMapping("/post")
    public ResponseEntity<Object> createPost(@RequestPart(value = "data") @Valid PostRequestDto postRequestDto, // @valid 객체 검증 수행
                                             @RequestPart(value = "image" ,required = false) @Valid MultipartFile image,
                                             @AuthenticationPrincipal UserDetailsImpl userDetails) throws IOException {

        PostResponseDto postResponseDto = postService.createPost(postRequestDto, image, userDetails.getMember());

        ResponseResource responseResource = new ResponseResource(postResponseDto);
        responseResource.add(linkTo(PostController.class).withSelfRel());
        responseResource.add(linkTo(PostController.class).slash(postResponseDto.getId()).withRel("post-get"));
        responseResource.add(linkTo(PostController.class).slash(postResponseDto.getId()).withRel("post-edit"));
        responseResource.add(linkTo(PostController.class).slash(postResponseDto.getId()).withRel("post-delete"));

        Response response = new Response(StatusEnum.CREATED, "게시글 작성 성공", responseResource);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }


    /**
     * 게시글 전체 조회
     * @param pageNum : 조회할 페이지 번호
     * @return :
     */
    @GetMapping("/post")
    public ResponseEntity<Object> getAllPosts(@RequestParam("page") int pageNum) {
        PostPageResponseDto postPageResponseDto = postService.getAllPosts(pageNum);

        ResponseResource responseResource = new ResponseResource(postPageResponseDto);
        responseResource.add(linkTo(PostController.class).withSelfRel());

        Response response = new Response(StatusEnum.OK, "게시글 조회 성공", responseResource);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 게시글 단건 조회
     * @param postId : 조회할 게시글 id
     * @return :
     */
    @GetMapping("/post/{postId}")
    public ResponseEntity<Object> getPost(@PathVariable Long postId) {
        PostResponseDto postResponseDto = postService.getPost(postId);

        ResponseResource responseResource = new ResponseResource(postResponseDto);
        responseResource.add(linkTo(PostController.class).slash(postId).withSelfRel());

        Response response = new Response(StatusEnum.OK, "게시글 조회 성공", responseResource);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 게시글 수정
     * @param postId : 수정할 게시글 id
     * @return :
     */
    @PutMapping("/post/{postId}")
    public ResponseEntity<Object> updatePost(@PathVariable Long postId,
                                             @RequestPart(value = "data") @Valid PostRequestDto postRequestDto,
                                             @RequestPart(value = "image" ,required = false) @Valid MultipartFile image,
                                             @AuthenticationPrincipal UserDetailsImpl userDetails) throws IOException {
        PostResponseDto postResponseDto = postService.updatePost(postId, postRequestDto, image, userDetails.getMember());

        ResponseResource responseResource = new ResponseResource(postResponseDto);
        responseResource.add(linkTo(PostController.class).slash(postId).withSelfRel());

        Response response = new Response(StatusEnum.OK, "게시글 수정 성공", responseResource);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 게시글 삭제
     * @param postId : 삭제할 게시글 id
     * @return :
     */
    @DeleteMapping( "/post/{postId}")
    public ResponseEntity<Object> deletePost(@PathVariable Long postId,
                                             @AuthenticationPrincipal UserDetailsImpl userDetails) throws IOException {
        postService.postDelete(postId, userDetails.getMember());

        ResponseResource responseResource = new ResponseResource(null);
        responseResource.add(linkTo(PostController.class).slash(postId).withSelfRel());

        Response response = new Response(StatusEnum.OK, "게시글 삭제 성공", responseResource);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 게시글 좋아요
     * @param postId : '좋아요' 할 게시글 id
     * @param userDetails : 게시글에 '좋아요'를 한 User
     * @return : 좋아요 성공 응답
     */
    @PostMapping("/post/heart/{postId}")
    public ResponseEntity<Object> addPostHeart(@PathVariable Long postId,
                                               @AuthenticationPrincipal UserDetailsImpl userDetails) {
        postService.addPostHeart(postId, userDetails.getMember());

        ResponseResource responseResource = new ResponseResource(null);
        responseResource.add(linkTo(PostController.class).withSelfRel());
        responseResource.add(linkTo(PostController.class).slash("heart").slash(postId).withRel("heart-delete"));

        Response response = new Response(StatusEnum.OK, "좋아요 성공", responseResource);

        return  new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 게시글 좋아요 취소
     * @param postId : 좋아요' 취소 할 게시글 id
     * @param userDetails 게시글에 '좋아요' 취소를 한 user
     * @return : 좋아요 취소 성공 응답
     */
    @DeleteMapping( "/post/heart/{postId}")
    public ResponseEntity<Object> deletePostHeart(@PathVariable Long postId,
                                                  @AuthenticationPrincipal UserDetailsImpl userDetails) {
        postService.deletePostHeart(postId, userDetails.getMember());

        ResponseResource responseResource = new ResponseResource(null);
        responseResource.add(linkTo(PostController.class).withSelfRel());
        responseResource.add(linkTo(PostController.class).slash("heart").slash(postId).withRel("heart-post"));

        Response response = new Response(StatusEnum.OK, "좋아요 취소 성공", responseResource);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
