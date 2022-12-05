package sideproject.petmeeting.member.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
}
