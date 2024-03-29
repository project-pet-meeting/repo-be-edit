package sideproject.petmeeting.member.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sideproject.petmeeting.chat.domain.ChatMember;
import sideproject.petmeeting.common.Timestamped;
import sideproject.petmeeting.meeting.domain.Attendance;
import sideproject.petmeeting.member.dto.request.MemberDetailRequestDto;
import sideproject.petmeeting.member.dto.request.MemberUpdateRequest;
import sideproject.petmeeting.pet.domain.Pet;
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
public class Member extends Timestamped {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;
    private String nickname;
    private String password;
    private String email;
    private String image;
    private String location;
    @Enumerated(value = STRING)
    private UserRole userRole;
    @OneToMany(mappedBy = "member")
    @Builder.Default
    private List<Post> post = new ArrayList<>();
    @OneToMany(mappedBy = "member")
    @Builder.Default
    private List<ChatMember> chatMembers = new ArrayList<>();
    @OneToMany(mappedBy = "member")
    @Builder.Default
    private List<Pet> pet = new ArrayList<>();
    @OneToMany(mappedBy = "member")
    @Builder.Default
    private List<Attendance> attendance = new ArrayList<>();


    public Member update(MemberUpdateRequest memberUpdateRequest) {
        this.nickname = memberUpdateRequest.getEmail();
        this.password = memberUpdateRequest.getPassword();
        this.email = memberUpdateRequest.getEmail();
        this.location = memberUpdateRequest.getLocation();
        return this;
    }

    public Member detail(MemberDetailRequestDto memberDetailRequestDto) {
        this.nickname = memberDetailRequestDto.getNickname();
        this.location = memberDetailRequestDto.getLocation();
        return this;
    }
}
