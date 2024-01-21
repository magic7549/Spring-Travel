package com.yong.traeblue.controller.plan;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/plan")
public class PlanViewController {
    @GetMapping("")
    public String planList() {
        return "plan/plan_list";
    }
}
