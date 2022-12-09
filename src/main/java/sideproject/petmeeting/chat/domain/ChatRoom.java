package sideproject.petmeeting.chat.domain;

import lombok.*;
import sideproject.petmeeting.post.domain.Post;

import javax.persistence.*;

import java.util.ArrayList;
import java.util.List;

import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ChatRoom {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;
    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "postId")
    private Post post;
    @OneToMany
    private List<ChatMember> chatMembers = new ArrayList<>();
    private String roomName;
}
