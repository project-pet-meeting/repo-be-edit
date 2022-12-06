package sideproject.petmeeting.comment.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import sideproject.petmeeting.comment.domain.Comment;
import sideproject.petmeeting.comment.dto.request.CommentRequestDto;
import sideproject.petmeeting.comment.dto.request.CommentUpdateRequest;
import sideproject.petmeeting.comment.dto.response.CommentResponseDto;
import sideproject.petmeeting.comment.repository.CommentRepository;
import sideproject.petmeeting.comment.service.CommentService;
import sideproject.petmeeting.common.Response;
import sideproject.petmeeting.common.ResponseResource;
import sideproject.petmeeting.common.StatusEnum;
import sideproject.petmeeting.post.repository.PostRepository;
import sideproject.petmeeting.post.domain.Post;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static sideproject.petmeeting.common.StatusEnum.CREATED;
import static sideproject.petmeeting.common.StatusEnum.OK;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/comment")
public class CommentController {

    private final CommentService commentService;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;


    @PostMapping(value = "/{postId}")
    public ResponseEntity createComment(@PathVariable Long postId,
                                        @RequestBody @Valid CommentRequestDto commentRequestDto,
                                        HttpServletRequest httpServletRequest, Errors errors) {
        Response message = new Response();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));

        if (errors.hasErrors()) {
            message.setStatus(StatusEnum.BAD_REQUEST);
            message.setMessage("다시 시도해주세요");
            message.setData(errors);
            return new ResponseEntity<>(message, headers, BAD_REQUEST);
        }
        Comment comment = commentService.createComment(postId, commentRequestDto, httpServletRequest);
        ResponseResource responseResource = new ResponseResource(comment.getId());
        responseResource.add(linkTo(CommentController.class).slash(postId).withSelfRel());
        responseResource.add(linkTo(CommentController.class).slash(comment.getId()).withRel("edit comment"));
        responseResource.add(linkTo(CommentController.class).slash(comment.getId()).withRel("get comment"));
        responseResource.add(linkTo(CommentController.class).slash(comment.getId()).withRel("delete comment"));

        message.setStatus(CREATED);
        message.setMessage("댓글 작성 완료되었습니다");
        message.setData(responseResource);
        return new ResponseEntity(message, headers, HttpStatus.CREATED);
    }

    @GetMapping(value = "/{postId}")
    public ResponseEntity getCommentList(@PathVariable Long postId,
                                         HttpServletRequest httpServletRequest
    ) {
        Response message = new Response();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));

        Optional<Post> optionalPost = postRepository.findById(postId);
        if (optionalPost.isEmpty()) {
            message.setStatus(StatusEnum.BAD_REQUEST);
            message.setMessage("해당 게시글이 존재하지 않습니다.");
            message.setData(postId);
            return new ResponseEntity(message, headers, BAD_REQUEST);
        }
        List<CommentResponseDto> commentList = commentService.getCommentList(postId, httpServletRequest);
        ResponseResource responseResource = new ResponseResource(commentList);
        responseResource.add(linkTo(CommentController.class).slash(postId).withSelfRel());

        message.setStatus(OK);
        message.setMessage("메세지 조회 완료");
        message.setData(responseResource);

        return new ResponseEntity(message, headers, HttpStatus.OK);
    }

    @PutMapping(value = "/{commentId}")
    public ResponseEntity editComment(@PathVariable Long commentId,
                                      @RequestBody CommentUpdateRequest commentUpdateRequest,
                                      HttpServletRequest httpServletRequest,
                                      Errors errors) {
        Response message = new Response();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));

        if (errors.hasErrors()) {
            message.setStatus(StatusEnum.BAD_REQUEST);
            message.setMessage("다시 시도해주세요");
            message.setData(errors);
            return new ResponseEntity<>(message, headers, BAD_REQUEST);
        }

        Comment comment = commentRepository.findById(commentId).orElse(null);
        if (comment == null) {
            message.setStatus(StatusEnum.BAD_REQUEST);
            message.setMessage("해당 댓글이 존재하지 않습니다.");
            message.setData(commentId);
            return new ResponseEntity(message, headers, BAD_REQUEST);
        }
        commentService.updateComment(commentId, commentUpdateRequest, httpServletRequest);
        ResponseResource responseResource = new ResponseResource(commentId);
        responseResource.add(linkTo(CommentController.class).slash(commentId).withSelfRel());
        responseResource.add(linkTo(CommentController.class).slash(commentId).withRel("delete comment"));
        message.setStatus(OK);
        message.setMessage("댓글 수정 완료");
        message.setData(responseResource);
        return new ResponseEntity(message, headers, HttpStatus.OK);
    }

    @DeleteMapping(value = "/{commentId}")
    public ResponseEntity deleteComment(@PathVariable Long commentId,
                                        HttpServletRequest httpServletRequest) {
        Response message = new Response();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));

        Comment comment = commentRepository.findById(commentId).orElse(null);
        if (comment == null) {
            message.setStatus(StatusEnum.BAD_REQUEST);
            message.setMessage("해당 댓글이 존재하지 않습니다.");
            message.setData(commentId);
            return new ResponseEntity(message, headers, BAD_REQUEST);
        }
        commentService.deleteComment(commentId, httpServletRequest);
        ResponseResource responseResource = new ResponseResource(commentId);
        responseResource.add(linkTo(CommentController.class).slash(commentId).withSelfRel());
        message.setStatus(OK);
        message.setMessage("댓글 삭제 완료");
        message.setData(responseResource);
        return new ResponseEntity(message, headers, HttpStatus.OK);
    }
}
