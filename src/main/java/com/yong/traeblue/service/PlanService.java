package com.yong.traeblue.service;

import com.yong.traeblue.config.exception.CustomException;
import com.yong.traeblue.config.exception.ErrorCode;
import com.yong.traeblue.domain.Destination;
import com.yong.traeblue.domain.Member;
import com.yong.traeblue.domain.Plan;
import com.yong.traeblue.dto.destination.DestinationResponseDto;
import com.yong.traeblue.dto.plans.MyPlanListResponseDto;
import com.yong.traeblue.dto.plans.PlanResponseDto;
import com.yong.traeblue.repository.MemberRepository;
import com.yong.traeblue.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
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
                .member(member)
                .title(title)
                .startDate(LocalDate.parse(startDate))
                .endDate(LocalDate.parse(endDate))
                .build()).getIdx();
    }

    // 내 계획 목록 조회
    public List<MyPlanListResponseDto> findAllMyPlan(Long memberIdx) {
        Member member = memberRepository.findById(memberIdx).orElseThrow(() -> new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_EXISTED_MEMBER));

        List<Plan> myPlans = planRepository.findByMember(member);

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

    // 계획 조회
    public PlanResponseDto findById(Long idx) {
        Plan plan = planRepository.findById(idx).orElseThrow(() -> new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_EXISTED_PLAN));

        List<DestinationResponseDto> destinationResponseDtos = null;
        if (plan.getDestinations() != null) {
            destinationResponseDtos  = plan.getDestinations().stream()
                    .map(destination  -> DestinationResponseDto.builder()
                            .content_idx(destination.getContentIdx())
                            .title(destination.getTitle())
                            .addr1(destination.getAddr1())
                            .addr2(destination.getAddr2())
                            .mapX(destination.getMapX())
                            .mapY(destination.getMapY())
                            .visitDate(destination.getVisitDate())
                            .orderNum(destination.getOrderNum())
                            .build())
                    .sorted(Comparator
                            .comparing(DestinationResponseDto::getVisitDate)
                            .thenComparing(DestinationResponseDto::getOrderNum))
                    .collect(Collectors.toList());
        }

        PlanResponseDto planResponseDto = PlanResponseDto.builder()
                .title(plan.getTitle())
                .startDate(String.valueOf(plan.getStartDate()))
                .endDate(String.valueOf(plan.getEndDate()))
                .destinations(destinationResponseDtos)
                .build();

        return planResponseDto;
    }
}
