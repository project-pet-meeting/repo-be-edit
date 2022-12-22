package sideproject.petmeeting.chat.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

import static javax.persistence.EnumType.STRING;

@Getter
@Setter
@Entity
public class ChatMessage {
    @Id
    @GeneratedValue
    private Long id;
    @Enumerated(value = STRING)
    private MessageType type; // 메시지 타입
    private String roomId; // 방번호
    private String sender; // 메시지 보낸사람
    private String message; // 메시지
}
