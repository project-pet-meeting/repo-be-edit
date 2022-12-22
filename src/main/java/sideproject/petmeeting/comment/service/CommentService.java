package sideproject.petmeeting.comment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sideproject.petmeeting.comment.domain.Comment;
import sideproject.petmeeting.comment.dto.request.CommentRequestDto;
import sideproject.petmeeting.comment.dto.request.CommentUpdateRequest;
import sideproject.petmeeting.comment.dto.response.CommentResponseDto;
import sideproject.petmeeting.comment.repository.CommentRepository;
import sideproject.petmeeting.common.exception.BusinessException;
import sideproject.petmeeting.member.domain.Member;
import sideproject.petmeeting.member.repository.MemberRepository;
import sideproject.petmeeting.post.repository.PostRepository;
import sideproject.petmeeting.post.domain.Post;
import sideproject.petmeeting.security.TokenProvider;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static sideproject.petmeeting.common.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CommentService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final TokenProvider tokenProvider;
    private final MemberRepository memberRepository;

    @Transactional
    public Comment createComment(Long postId, CommentRequestDto commentRequestDto, HttpServletRequest httpServletRequest) {

        Member member = checkAuthentication(httpServletRequest);

        Optional<Post> optionalPost = postRepository.findById(postId);
        checkPostExistence(optionalPost);
        Comment comment = buildComment(commentRequestDto, member, optionalPost);
        Comment savedComment = commentRepository.save(comment);
        return savedComment;
    }

    public List<CommentResponseDto> getCommentList(Long postId, HttpServletRequest httpServletRequest) {
        Member member = checkAuthentication(httpServletRequest);
        List<CommentResponseDto> commentResponseDtoList = new ArrayList<>();
        List<Comment> commentList =commentRepository.findAllByPostId(postId);
        buildCommentList(commentResponseDtoList, commentList);

        return commentResponseDtoList;

    }

    @Transactional
    public void updateComment(Long commentId, CommentUpdateRequest commentUpdateRequest, HttpServletRequest httpServletRequest) {
        checkAuthentication(httpServletRequest);
        Comment comment = commentRepository.findById(commentId).get();
        comment.update(commentUpdateRequest);
    }

    @Transactional
    public void deleteComment(Long commentId, HttpServletRequest httpServletRequest) {
        checkAuthentication(httpServletRequest);
        commentRepository.deleteById(commentId);
    }

    private static void checkPostExistence(Optional<Post> optionalPost) {
        if (optionalPost.isEmpty()) {
            throw new BusinessException("존재하지 않는 게시글 입니다.", ENTITY_NOT_FOUND);
        }
    }

    private static Comment buildComment(CommentRequestDto commentRequestDto, Member member, Optional<Post> optionalPost) {
        Comment comment = Comment.builder()
                .member(member)
                .post(optionalPost.get())
                .content(commentRequestDto.getContent())
                .build();
        return comment;
    }


    private Member checkAuthentication(HttpServletRequest httpServletRequest) {
        if (httpServletRequest.getHeader("Authorization") == null || httpServletRequest.getHeader("Authorization").isEmpty()) {
            throw new BusinessException("로그인이 필요합니다.", NEED_LOGIN);
        }
        String accessToken = httpServletRequest.getHeader("Authorization").substring(7);
        tokenProvider.validateToken(accessToken);
        Member member = memberRepository.findByEmail(tokenProvider.getUserEmailByToken(accessToken)).orElse(null);
        if (member == null) {
            throw new BusinessException("올바르지 않은 토큰입니다.", INVALID_TOKEN);
        }
        return member;
    }

    private static void buildCommentList(List<CommentResponseDto> commentResponseDtoList, List<Comment> commentList) {
        for (Comment comment : commentList) {
            commentResponseDtoList.add(CommentResponseDto.builder()
                    .id(comment.getId())
                    .content(comment.getContent())
                    .build());
        }
    }
}
