package com.yong.traeblue.service;

import com.yong.traeblue.config.exception.CustomException;
import com.yong.traeblue.config.exception.ErrorCode;
import com.yong.traeblue.domain.Member;
import com.yong.traeblue.domain.Plan;
import com.yong.traeblue.dto.plans.MyPlanListResponseDto;
import com.yong.traeblue.repository.MemberRepository;
import com.yong.traeblue.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlanService {
    private final PlanRepository planRepository;
    private final MemberRepository memberRepository;

    // 계획 생성
    public Long createPlan(Long memberIdx, String title, String startDate, String endDate) {
        Member member = memberRepository.findById(memberIdx).orElseThrow(() -> new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_EXISTED_MEMBER));

        return planRepository.save(Plan.builder()
                .memberIdx(member.getIdx())
                .title(title)
                .startDate(LocalDate.parse(startDate))
                .endDate(LocalDate.parse(endDate))
                .build()).getIdx();
    }

    // 내 계획 목록 조회
    public List<MyPlanListResponseDto> findAllMyPlan(Long memberIdx) {
        List<Plan> myPlans = planRepository.findByMemberIdx(memberIdx);

        // Plan 리스트를 MyPlanListResponseDto 리스트로
        List<MyPlanListResponseDto> myPlanListResponseDto = myPlans.stream()
                .map(plan -> MyPlanListResponseDto.builder()
                        .idx(plan.getIdx())
                        .title(plan.getTitle())
                        .startDate(plan.getStartDate().toString())
                        .endDate(plan.getEndDate().toString())
                        .build())
                .collect(Collectors.toList());

        return myPlanListResponseDto;
    }
}
