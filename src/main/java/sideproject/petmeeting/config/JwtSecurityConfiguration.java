package sideproject.petmeeting.config;

import lombok.RequiredArgsConstructor;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import sideproject.petmeeting.security.JwtFilter;
import sideproject.petmeeting.security.TokenProvider;
import sideproject.petmeeting.security.UserDetailsServiceImpl;

@RequiredArgsConstructor
public class JwtSecurityConfiguration extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {

    private final String SECRET_KEY;
    private final TokenProvider tokenProvider;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    public void configure(HttpSecurity builder) throws Exception {
        JwtFilter customJwtFilter = new JwtFilter(SECRET_KEY, tokenProvider, userDetailsService);
        builder.addFilterBefore(customJwtFilter, UsernamePasswordAuthenticationFilter.class);
    }
}
