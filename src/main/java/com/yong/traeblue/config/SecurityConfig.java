package com.yong.traeblue.config;

import com.yong.traeblue.config.jwt.JWTFilter;
import com.yong.traeblue.config.jwt.JWTUtil;
import com.yong.traeblue.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final AuthenticationConfiguration authenticationConfiguration;
    private final JWTUtil jwtUtil;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final RefreshTokenRepository refreshTokenRepository;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf((csrf) -> csrf.disable());
        http.httpBasic((httpBasic) -> httpBasic.disable());
        http.formLogin((login) -> login.disable());
        http.sessionManagement((sessionManagement) -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.exceptionHandling((exception) -> exception.authenticationEntryPoint(customAuthenticationEntryPoint));
        http.exceptionHandling((exception) -> exception.accessDeniedHandler(customAccessDeniedHandler));

        http.addFilterBefore(new JWTFilter(jwtUtil, refreshTokenRepository), LoginFilter.class);
        http.addFilterAt(new LoginFilter(authenticationManager(authenticationConfiguration), jwtUtil, refreshTokenRepository), UsernamePasswordAuthenticationFilter.class);

        http.authorizeHttpRequests((authorizeHttpRequests) -> authorizeHttpRequests
                .requestMatchers(
                "/static/**",
                        "/img/**",
                        "/css/**",
                        "/",
                        "/member/login",
                        "/member/find-username",
                        "/member/find-password",
                        "/member/signup",
                        "/api/v1/member/login",
                        "/api/v1/member/username/**",
                        "/api/v1/member/find-username",
                        "/api/v1/member/find-password",
                        "/api/v1/member/signup"
                ).permitAll()
                .anyRequest().authenticated());

        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
