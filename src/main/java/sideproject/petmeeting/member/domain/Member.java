package sideproject.petmeeting.member.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sideproject.petmeeting.member.dto.request.MemberUpdateRequest;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import static javax.persistence.GenerationType.IDENTITY;
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Entity
public class Member{
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;
    private String nickname;
    private String password;
    private String email;
    private String image;

    public Member update(MemberUpdateRequest memberUpdateRequest) {
        this.nickname = memberUpdateRequest.getEmail();
        this.password = memberUpdateRequest.getPassword();
        this.email = memberUpdateRequest.getEmail();
        this.image = memberUpdateRequest.getImage();
        return this;
    }
}
