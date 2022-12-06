package sideproject.petmeeting.token.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sideproject.petmeeting.member.domain.Member;

import javax.persistence.*;

import static javax.persistence.FetchType.LAZY;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class RefreshToken {

    @Id
    private String id;

    @JoinColumn(name = "member_id", nullable = false)
    @OneToOne(fetch = LAZY)
    private Member member;

    @Column(nullable = false)
    private String keyValue;
}
