package com.yong.traeblue.controller.member;

import com.yong.traeblue.dto.member.AddMemberRequestDto;
import com.yong.traeblue.dto.member.FindPasswordRequestDto;
import com.yong.traeblue.dto.member.FindUsernameRequestDto;
import com.yong.traeblue.repository.RefreshTokenRepository;
import com.yong.traeblue.service.MemberService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1/member")
public class MemberApiController {
    private final MemberService memberService;
    private final RefreshTokenRepository refreshTokenRepository;

    // 아이디 중복체크
    @ResponseBody
    @GetMapping("/username/{username}")
    public ResponseEntity<Map<String, Boolean>> checkUsername(@PathVariable(name = "username") String username) {
        boolean isExists = memberService.existsUsername(username);
        // 사용 가능하면 true, 아니면 false
        return ResponseEntity.ok().body(Collections.singletonMap("isAvailable", !isExists));
    }

    // 회원가입
    @PostMapping("/signup")
    public String signup(AddMemberRequestDto addMember) throws UnsupportedEncodingException {
        if (memberService.save(addMember))
            return "redirect:/member/login";
        else {
            String errorMessage = URLEncoder.encode("회원가입에 실패하였습니다.", "UTF-8");
            return "redirect:/member/signup?error=true&exception=" + errorMessage;
        }
    }

    // 아이디 찾기
    @ResponseBody
    @PostMapping("/find-username")
    public ResponseEntity<Map<String, String>> findUsername(FindUsernameRequestDto request) {
        String username = memberService.findUsername(request.getEmail(), request.getPhone());
        if (username != null) {
            return ResponseEntity.ok().body(Collections.singletonMap("username", username));
        }
        else {
            return ResponseEntity.notFound().build();
        }
    }

    // 비밀번호 찾기 - 임시 비밀번호 발급
    @ResponseBody
    @PostMapping("/find-password")
    public ResponseEntity<Map<String, String>> findPassword(FindPasswordRequestDto request) {
        String tempPassword = memberService.tempPassword(request.getUsername(), request.getEmail(), request.getPhone());
        if (tempPassword != null) {
            return ResponseEntity.ok().body(Collections.singletonMap("tempPassword", tempPassword));
        }
        else {
            return ResponseEntity.notFound().build();
        }
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
