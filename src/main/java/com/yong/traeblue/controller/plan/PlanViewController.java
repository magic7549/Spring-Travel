package com.yong.traeblue.controller.plan;

import com.yong.traeblue.config.jwt.JWTUtil;
import com.yong.traeblue.dto.plans.MyPlanListResponseDto;
import com.yong.traeblue.dto.plans.PlanResponseDto;
import com.yong.traeblue.service.PlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequiredArgsConstructor
@Controller
public class PlanViewController {
    private final PlanService planService;
    private final JWTUtil jwtUtil;

    @GetMapping("/plans")
    public String plans(Model model, @CookieValue(name = "access") String accessToken) {
        Long memberIdx = jwtUtil.getIdx(accessToken);

        List<MyPlanListResponseDto> myPlans = planService.findAllMyPlan(memberIdx);

        model.addAttribute("myPlans", myPlans);

        return "plans/plan_list";
    }

    @GetMapping("/plans/{idx}")
    public String planDetail(@PathVariable(value = "idx") Long idx, Model model) {
        PlanResponseDto planDetail = planService.findById(idx);

        model.addAttribute("planDetail", planDetail);
        model.addAttribute("travelDuration", planDetail.getTravelDuration());

        return "plans/plan_view";
    }
}
