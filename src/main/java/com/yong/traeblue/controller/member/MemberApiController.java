package com.yong.traeblue.controller.member;

import com.yong.traeblue.config.jwt.JWTUtil;
import com.yong.traeblue.dto.member.AddMemberRequestDto;
import com.yong.traeblue.dto.member.ChangePasswordRequestDto;
import com.yong.traeblue.dto.member.FindPasswordRequestDto;
import com.yong.traeblue.dto.member.FindUsernameRequestDto;
import com.yong.traeblue.repository.RefreshTokenRepository;
import com.yong.traeblue.service.MemberService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<Map<String, Boolean>> signup(@RequestBody AddMemberRequestDto addMember) {
        if (memberService.save(addMember.getUsername(), addMember.getPassword(), addMember.getEmail(), addMember.getPhone()))
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
    public ResponseEntity<Map<String, Boolean>> changePassword(HttpServletRequest request, @RequestBody ChangePasswordRequestDto passwordDto) {
        // access 쿠키에서 username 추출
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("access")) {
                    String accessValue = cookie.getValue();
                    String username = jwtUtil.getUsername(accessValue);

                    boolean isSuccess = memberService.changePassword(passwordDto.getCurrentPassword(), passwordDto.getNewPassword(), username);
                    return ResponseEntity.ok().body(Collections.singletonMap("isSuccess", isSuccess));
                }
            }
        }
        return ResponseEntity.ok().body(Collections.singletonMap("isSuccess", false));
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
}
