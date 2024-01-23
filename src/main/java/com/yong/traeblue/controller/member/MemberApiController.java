package com.yong.traeblue.controller.member;

import com.yong.traeblue.config.exception.CustomException;
import com.yong.traeblue.config.exception.ErrorCode;
import com.yong.traeblue.config.jwt.JWTUtil;
import com.yong.traeblue.dto.member.*;
import com.yong.traeblue.repository.RefreshTokenRepository;
import com.yong.traeblue.service.MemberService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/member")
public class MemberApiController {
    private final MemberService memberService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JWTUtil jwtUtil;

    // 아이디 중복체크
    @GetMapping("/username/{username}")
    public ResponseEntity<Map<String, Boolean>> checkUsername(@PathVariable(name = "username") String username) {
        boolean isExists = memberService.existsUsername(username);
        // 사용 가능하면 true, 아니면 false
        return ResponseEntity.ok().body(Collections.singletonMap("isAvailable", !isExists));
    }

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<Map<String, Boolean>> signup(@RequestBody AddMemberRequestDto request) {
        if (memberService.save(request.getUsername(), request.getPassword(), request.getEmail(), request.getPhone()))
            return ResponseEntity.ok().body(Collections.singletonMap("isSuccess", true));
        else
            return ResponseEntity.ok().body(Collections.singletonMap("isSuccess", false));
    }

    // 아이디 찾기
    @PostMapping("/find-username")
    public ResponseEntity<Map<String, String>> findUsername(@RequestBody FindUsernameRequestDto request) {
        String username = memberService.findUsername(request.getEmail(), request.getPhone());
        return ResponseEntity.ok().body(Collections.singletonMap("username", username));
    }

    // 비밀번호 찾기 - 임시 비밀번호 발급
    @PostMapping("/find-password")
    public ResponseEntity<Map<String, String>> findPassword(@RequestBody FindPasswordRequestDto request) {
        String tempPassword = memberService.tempPassword(request.getUsername(), request.getEmail(), request.getPhone());
        return ResponseEntity.ok().body(Collections.singletonMap("tempPassword", tempPassword));
    }

    // 비밀번호 변경
    @PutMapping("/password")
    public ResponseEntity<Map<String, Boolean>> changePassword(@CookieValue(name = "access", required = false) String accessToken, @RequestBody ChangePasswordRequestDto passwordDto) {
        if (accessToken == null) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, ErrorCode.ACCESS_DENIED);
        }

        String username = jwtUtil.getUsername(accessToken);
        boolean isSuccess = memberService.changePassword(passwordDto.getCurrentPassword(), passwordDto.getNewPassword(), username);
        return ResponseEntity.ok().body(Collections.singletonMap("isSuccess", isSuccess));
    }

    // 로그아웃
    @GetMapping("/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // access 쿠키 삭제
        Cookie accessCookie = new Cookie("access", null);
        accessCookie.setMaxAge(0);
        accessCookie.setPath("/");
        response.addCookie(accessCookie);

        // refresh 쿠키 및 db 삭제
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("refresh")) {
                    String refreshValue = cookie.getValue();
                    refreshTokenRepository.deleteById(refreshValue);

                    // 쿠키 삭제
                    cookie.setMaxAge(0);
                    cookie.setPath("/");
                    response.addCookie(cookie);
                }
            }
        }

        response.sendRedirect("/");
    }

    // 회원 탈퇴
    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, Boolean>> withdraw(@RequestBody WithdrawMemberRequestDto request, HttpServletResponse response, @CookieValue(name = "refresh") String refresh) {
        if (memberService.withdrawMember(request.getUsername(), request.getPassword())) {
            // access 쿠키 삭제
            Cookie accessCookie = new Cookie("access", null);
            accessCookie.setMaxAge(0);
            accessCookie.setPath("/");
            response.addCookie(accessCookie);

            // refresh 쿠키 삭제
            refreshTokenRepository.deleteById(refresh);
            Cookie refreshCookie = new Cookie("refresh", null);
            refreshCookie.setMaxAge(0);
            refreshCookie.setPath("/");
            response.addCookie(refreshCookie);

            return ResponseEntity.ok().body(Collections.singletonMap("isSuccess", true));
        }
        else
            return ResponseEntity.ok().body(Collections.singletonMap("isSuccess", false));
    }
}
