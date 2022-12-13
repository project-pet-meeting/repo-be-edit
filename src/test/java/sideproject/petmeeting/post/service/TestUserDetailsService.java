package sideproject.petmeeting.post.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import sideproject.petmeeting.member.domain.Member;
import sideproject.petmeeting.security.UserDetailsImpl;


import static sideproject.petmeeting.member.domain.UserRole.ROLE_MEMBER;

public class TestUserDetailsService implements UserDetailsService {

    public static final String USERNAME = "user@example.com";
    public static final String PASSWORD = "password";

    private Member getMember() {
        return Member.builder()
                .id(1L)
                .nickname(USERNAME)
                .password(PASSWORD)
                .email(USERNAME)
                .image("test-image")
                .userRole(ROLE_MEMBER)
                .build();
    }

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        if (s.equals(USERNAME)) {
            return new UserDetailsImpl(getMember()) {
            };
        }
        return null;
    }
}
