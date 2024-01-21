package com.yong.traeblue.controller.member;

import com.yong.traeblue.dto.member.AddMemberRequestDto;
import com.yong.traeblue.dto.member.FindUsernameRequestDto;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/member")
public class MemberViewController {

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "exception", required = false) String exception,
                        Model model) {
        model.addAttribute("error", error);
        model.addAttribute("exception", exception);

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
    public String signup(@RequestParam(value = "error", required = false) String error,
                         @RequestParam(value = "exception", required = false) String exception,
                         Model model, AddMemberRequestDto addMember) {
        model.addAttribute("addMember", addMember);
        model.addAttribute("error", error);
        model.addAttribute("exception", exception);

        return "member/signup";
    }
}
