package com.yong.traeblue.config;

import com.yong.traeblue.config.jwt.JWTUtil;
import com.yong.traeblue.domain.RefreshToken;
import com.yong.traeblue.dto.CustomUserDetails;
import com.yong.traeblue.repository.RefreshTokenRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Iterator;

public class LoginFilter extends UsernamePasswordAuthenticationFilter {
    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    public LoginFilter(AuthenticationManager authenticationManager, JWTUtil jwtUtil, RefreshTokenRepository refreshTokenRepository) {
        super.setFilterProcessesUrl("/api/v1/member/login");
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        String username = obtainUsername(request);
        String password = obtainPassword(request);

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, password, null);

        return authenticationManager.authenticate(authToken);
    }

    // 로그인 성공 시 실행
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) throws IOException, ServletException {
        CustomUserDetails customUserDetails = (CustomUserDetails)authentication.getPrincipal();

        String username = customUserDetails.getUsername();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();

        String role = auth.getAuthority();

        // access token 생성
        String accessValue = jwtUtil.createAccess(username, role);
        Cookie accessCookie = new Cookie("access", accessValue);
        accessCookie.setHttpOnly(true);
        accessCookie.setMaxAge((int) jwtUtil.getAccessExpireTime());
        accessCookie.setSecure(false);
        accessCookie.setPath("/");
        response.addCookie(accessCookie);

        // refresh token 생성
        String refreshValue = jwtUtil.createRefresh(username, role);

        RefreshToken refreshToken = new RefreshToken(refreshValue, username);
        refreshTokenRepository.save(refreshToken);

        System.out.println(refreshTokenRepository.findById(refreshValue));

        Cookie refreshCookie = new Cookie("refresh", refreshValue);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setMaxAge((int) jwtUtil.getRefreshExpireTime());
        refreshCookie.setSecure(false);
        refreshCookie.setPath("/");
        response.addCookie(refreshCookie);

        response.sendRedirect("/");
    }

    // 로그인 실패 시 실행
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        String errorMessage;

        if(failed instanceof BadCredentialsException) {
            errorMessage = "아이디 또는 비밀번호가 맞지 않습니다.";
        }  else {
            errorMessage = "로그인 요청을 처리할 수 없습니다.";
        }

        errorMessage = URLEncoder.encode(errorMessage, "UTF-8");
        response.sendRedirect("/member/login?error=true&exception=" + errorMessage);
    }
}
