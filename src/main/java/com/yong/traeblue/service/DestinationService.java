package com.yong.traeblue.service;

import com.yong.traeblue.config.exception.CustomException;
import com.yong.traeblue.config.exception.ErrorCode;
import com.yong.traeblue.domain.Destination;
import com.yong.traeblue.domain.Plan;
import com.yong.traeblue.dto.destination.AddDestinationRequestDto;
import com.yong.traeblue.repository.DestinationRepository;
import com.yong.traeblue.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DestinationService {
    private final DestinationRepository destinationRepository;
    private final PlanRepository planRepository;

    // 목적지 저장
    public boolean save(AddDestinationRequestDto requestDto) {
        Plan plan = planRepository.findById(requestDto.getPlanIdx()).orElseThrow(() -> new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_EXISTED_PLAN));

        Destination destination = Destination.builder()
                .plan(plan)
                .contentIdx(requestDto.getContentIdx())
                .title(requestDto.getTitle())
                .addr1(requestDto.getAddr1())
                .addr2(requestDto.getAddr2())
                .mapX(requestDto.getMapX())
                .mapY(requestDto.getMapY())
                .visitDate(requestDto.getVisitDate())
                .orderNum(requestDto.getOrderNum())
                .build();

        try {
            destinationRepository.save(destination);
            return true;
        } catch (CustomException e) {
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.UNKNOWN_ADD_DESTINATION);
        }
    }
}
