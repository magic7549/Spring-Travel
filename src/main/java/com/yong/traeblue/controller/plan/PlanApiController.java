package com.yong.traeblue.controller.plan;

import com.yong.traeblue.config.jwt.JWTUtil;
import com.yong.traeblue.dto.plans.CreatePlanRequestDto;
import com.yong.traeblue.dto.plans.MyPlanListResponseDto;
import com.yong.traeblue.dto.plans.PlanResponseDto;
import com.yong.traeblue.service.PlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class PlanApiController {
    private final PlanService planService;
    private final JWTUtil jwtUtil;

    // 계획 생성
    @PostMapping("/plans")
    public ResponseEntity<Map<String, Long>> createPlan(@CookieValue(name = "access") String accessToken, @RequestBody CreatePlanRequestDto requestDto) {
        Long memberIdx = jwtUtil.getIdx(accessToken);

        Long planIdx = planService.createPlan(memberIdx, requestDto.getTitle(), requestDto.getStartDate(), requestDto.getEndDate());

        return ResponseEntity.ok().body(Collections.singletonMap("planIdx", planIdx));
    }

    // 내 계획 목록 조회
    @GetMapping("/plans")
    public ResponseEntity<List<MyPlanListResponseDto>> findMyPlanList(@CookieValue(name = "access") String accessToken) {
        Long memberIdx = jwtUtil.getIdx(accessToken);
        return ResponseEntity.ok().body(planService.findAllMyPlan(memberIdx));
    }

    // 계획 조회
    @GetMapping("/plans/{idx}")
    public ResponseEntity<PlanResponseDto> getPlanDetail(@PathVariable(name = "idx") Long idx) {
        return ResponseEntity.ok().body(planService.findById(idx));
    }
}
