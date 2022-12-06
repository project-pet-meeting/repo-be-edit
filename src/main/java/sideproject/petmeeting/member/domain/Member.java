package sideproject.petmeeting.member.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sideproject.petmeeting.member.dto.request.MemberUpdateRequest;
import sideproject.petmeeting.post.domain.Post;

import javax.persistence.*;

import java.util.ArrayList;
import java.util.List;

import static javax.persistence.EnumType.STRING;
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
    @Enumerated(value = STRING)
    private UserRole userRole;
    @OneToMany(mappedBy = "member")
    private List<Post> post = new ArrayList<>();

    public Member update(MemberUpdateRequest memberUpdateRequest) {
        this.nickname = memberUpdateRequest.getEmail();
        this.password = memberUpdateRequest.getPassword();
        this.email = memberUpdateRequest.getEmail();
        this.image = memberUpdateRequest.getImage();
        return this;
    }
}
