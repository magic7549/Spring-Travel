package com.yong.traeblue.controller.member;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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

    @GetMapping("/mypage/password")
    public String changePassword() {
        return "member/mypage_password";
    }

    @GetMapping("/mypage/withdraw")
    public String memberWithdraw() {
        return "member/mypage_withdraw";
    }
}
