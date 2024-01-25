package com.yong.traeblue.config.jwt;

import com.yong.traeblue.config.exception.CustomException;
import com.yong.traeblue.config.exception.ErrorCode;
import com.yong.traeblue.domain.Member;
import com.yong.traeblue.domain.RefreshToken;
import com.yong.traeblue.dto.CustomUserDetails;
import com.yong.traeblue.repository.MemberRepository;
import com.yong.traeblue.repository.RefreshTokenRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Optional;

@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {
    private final JWTUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 쿠키에서 토큰 추출
        String accessToken = extractTokenFromCookie(request, "access");

        // 토큰이 없을 경우
        if (accessToken == null) {
            filterChain.doFilter(request, response);

            return;
        }

        // access 토큰이 만료되었을 경우
        if (jwtUtil.isExpired(accessToken)) {
            String refreshValue = extractTokenFromCookie(request, "refresh");

            // refresh 토큰이 만료되었을 경우
            if (jwtUtil.isExpired(refreshValue)) {
                Cookie cookie = new Cookie("access", null);
                cookie.setMaxAge(0);
                cookie.setPath("/");
                response.addCookie(cookie);

                cookie = new Cookie("refresh", null);
                cookie.setMaxAge(0);
                cookie.setPath("/");
                response.addCookie(cookie);

                filterChain.doFilter(request, response);
                return;
            }

            boolean existsRefresh = refreshTokenRepository.existsById(refreshValue);
            // refreshToken이 존재
            if (existsRefresh) {
                // access 쿠키 재발급
                accessToken = jwtUtil.createAccess(jwtUtil.getIdx(refreshValue), jwtUtil.getUsername(refreshValue), jwtUtil.getRole(refreshValue));
                Cookie cookie = new Cookie("access", accessToken);
                cookie.setHttpOnly(true);
                cookie.setMaxAge((int) jwtUtil.getAccessExpireTime());
                cookie.setSecure(false);
                cookie.setPath("/");
                response.addCookie(cookie);
            } else {
                Cookie cookie = new Cookie("access", null);
                cookie.setMaxAge(0);
                cookie.setPath("/");
                response.addCookie(cookie);

                cookie = new Cookie("refresh", null);
                cookie.setMaxAge(0);
                cookie.setPath("/");
                response.addCookie(cookie);

                filterChain.doFilter(request, response);
                return;
            }
        }

        // 토큰 유효한지 검증
        if (!jwtUtil.validateToken(accessToken)) {
            Cookie cookie = new Cookie("access", null);
            cookie.setMaxAge(0);
            cookie.setPath("/");
            response.addCookie(cookie);

            filterChain.doFilter(request, response);

            return;
        }

        String username = jwtUtil.getUsername(accessToken);
        String role = jwtUtil.getRole(accessToken);

        Member member = Member.builder()
                .username(username)
                .password("temp")
                .role(role)
                .build();

        CustomUserDetails customUserDetails = new CustomUserDetails(member);

        // 스프링 시큐리티 인증 토큰 생성
        Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
        // 세션에 사용자 등록
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }

    private String extractTokenFromCookie(HttpServletRequest request, String cookieName) {
        // 쿠키 배열 가져오기
        Cookie[] cookies = request.getCookies();

        // 쿠키 배열이 null이 아니고 쿠키의 이름이 cookieName인 경우 토큰 반환
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null; // 쿠키에서 토큰을 찾지 못한 경우 null 반환
    }
}
