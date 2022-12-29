package sideproject.petmeeting.chat.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sideproject.petmeeting.common.Timestamped;
import sideproject.petmeeting.member.domain.Member;

import javax.persistence.*;

import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMember extends Timestamped {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "chatRoomId")
    private ChatRoom chatRoom;
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "memberId")
    private Member member;
}
