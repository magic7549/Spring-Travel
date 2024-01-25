package com.yong.traeblue.controller.member;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/members")
public class MemberViewController {

    @GetMapping("/login")
    public String login() {
        return "members/login";
    }

    @GetMapping("/find-username")
    public String findUsername() {
        return "members/find_username";
    }

    @GetMapping("/find-password")
    public String findPassword() {
        return "members/find_password";
    }

    @GetMapping("/signup")
    public String signup() {
        return "members/signup";
    }

    @GetMapping("/mypage/password")
    public String changePassword() {
        return "members/mypage_password";
    }

    @GetMapping("/mypage/withdraw")
    public String memberWithdraw() {
        return "members/mypage_withdraw";
    }
}
