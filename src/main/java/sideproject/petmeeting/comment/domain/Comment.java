package sideproject.petmeeting.comment.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sideproject.petmeeting.comment.dto.request.CommentUpdateRequest;
import sideproject.petmeeting.member.domain.Member;
import sideproject.petmeeting.post.domain.Post;

import javax.persistence.*;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Comment {
    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;
    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;
    private String content;

    public void update(CommentUpdateRequest commentUpdateRequest) {
        this.content = commentUpdateRequest.getContent();
    }
}
