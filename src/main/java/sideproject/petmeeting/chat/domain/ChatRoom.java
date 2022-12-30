package sideproject.petmeeting.chat.domain;

import lombok.*;
import sideproject.petmeeting.common.Timestamped;
import sideproject.petmeeting.meeting.domain.Meeting;
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
public class ChatRoom extends Timestamped {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;
    private String roomId;
    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "meetingId")
    private Meeting meeting;
    @OneToMany
    private List<ChatMember> chatMembers = new ArrayList<>();
    private String roomName;
}
