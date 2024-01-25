package com.yong.traeblue.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yong.traeblue.config.exception.CustomException;
import com.yong.traeblue.config.exception.ErrorCode;
import com.yong.traeblue.config.jwt.JWTUtil;
import com.yong.traeblue.domain.RefreshToken;
import com.yong.traeblue.dto.CustomUserDetails;
import com.yong.traeblue.repository.RefreshTokenRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class LoginFilter extends UsernamePasswordAuthenticationFilter {
    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    public LoginFilter(AuthenticationManager authenticationManager, JWTUtil jwtUtil, RefreshTokenRepository refreshTokenRepository) {
        super.setFilterProcessesUrl("/api/v1/members/login");
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {
            // Request에서 InputStream을 통해 JSON 데이터를 읽어온다.
            InputStream inputStream = request.getInputStream();
            Map<String, String> jsonRequest = new ObjectMapper().readValue(inputStream, new TypeReference<Map<String, String>>() {});

            String username = jsonRequest.get("username");
            String password = jsonRequest.get("password");

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, password, null);

            return authenticationManager.authenticate(authToken);
        } catch (IOException e) {
            throw new AuthenticationServiceException("Failed to parse JSON authentication request", e);
        }
    }

    // 로그인 성공 시 실행
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) throws IOException, ServletException {
        CustomUserDetails customUserDetails = (CustomUserDetails)authentication.getPrincipal();

        String username = customUserDetails.getUsername();
        Long idx = customUserDetails.getIdx();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();

        String role = auth.getAuthority();

        // access token 생성
        String accessValue = jwtUtil.createAccess(idx, username, role);
        Cookie accessCookie = new Cookie("access", accessValue);
        accessCookie.setHttpOnly(true);
        accessCookie.setMaxAge((int) jwtUtil.getAccessExpireTime());
        accessCookie.setSecure(false);
        accessCookie.setPath("/");
        response.addCookie(accessCookie);

        // refresh token 생성
        String refreshValue = jwtUtil.createRefresh(idx, username, role);

        RefreshToken refreshToken = new RefreshToken(refreshValue, username);
        refreshTokenRepository.save(refreshToken);

        Cookie refreshCookie = new Cookie("refresh", refreshValue);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setMaxAge((int) jwtUtil.getRefreshExpireTime());
        refreshCookie.setSecure(false);
        refreshCookie.setPath("/");
        response.addCookie(refreshCookie);

        response.setStatus(HttpServletResponse.SC_OK);
    }

    // 로그인 실패 시 실행
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        if(failed instanceof BadCredentialsException) {
            // 400 에러 코드 설정
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("code", ErrorCode.INVALID_LOGIN.getCode());
            errorResponse.put("msg", ErrorCode.INVALID_LOGIN.getMsg());

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(new ObjectMapper().writeValueAsString(errorResponse));
        }  else {
            // 401 에러 코드 설정
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("code", ErrorCode.ACCESS_DENIED.getCode());
            errorResponse.put("msg", ErrorCode.ACCESS_DENIED.getMsg());

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(new ObjectMapper().writeValueAsString(errorResponse));
        }
    }
}
