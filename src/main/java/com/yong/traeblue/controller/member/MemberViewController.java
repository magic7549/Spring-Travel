package com.yong.traeblue.controller.member;

import com.yong.traeblue.dto.member.AddMemberRequestDto;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/member")
public class MemberViewController {

    @GetMapping("/login")
    public String login() {
        return "member/login";
    }

    @GetMapping("/find-username")
    public String findUsername() {
        return "member/find_username";
    }

    @GetMapping("/find-password")
    public String findPassword() {
        return "member/find_password";
    }

    @GetMapping("/signup")
    public String signup() {
        return "member/signup";
    }

    @GetMapping("/mypage")
    public String mypage() {
        return "member/mypage_password";
    }
}
