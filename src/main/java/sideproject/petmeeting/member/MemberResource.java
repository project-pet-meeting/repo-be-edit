package sideproject.petmeeting.member;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import org.springframework.hateoas.EntityModel;
import sideproject.petmeeting.member.domain.Member;

public class MemberResource extends EntityModel<Member> {

    @JsonUnwrapped
    private Member member;

    public MemberResource(Member member) {
        this.member = member;
    }

    public Member getMember() {
        return member;
    }
}
