package com.yong.traeblue.service;

import com.yong.traeblue.config.exception.CustomException;
import com.yong.traeblue.config.exception.ErrorCode;
import com.yong.traeblue.domain.Destination;
import com.yong.traeblue.domain.Plan;
import com.yong.traeblue.dto.destination.AddDestinationRequestDto;
import com.yong.traeblue.dto.destination.SaveDestinationRequestDto;
import com.yong.traeblue.repository.DestinationRepository;
import com.yong.traeblue.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DestinationService {
    private final DestinationRepository destinationRepository;
    private final PlanRepository planRepository;

    // 목적지 추가
    public boolean addDestination(AddDestinationRequestDto requestDto) {
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

    // 목적지 리스트 업데이트
    public boolean updateDestinations(Long planIdx, List<SaveDestinationRequestDto> requestDtoList) {
        Plan plan = planRepository.findById(planIdx).orElseThrow(() -> new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_EXISTED_PLAN));

        List<Destination> beforeDestinations = plan.getDestinations();
        for (int i = 0; i < beforeDestinations.size(); i++) {
            Destination destination = beforeDestinations.get(i);
            SaveDestinationRequestDto requestDto = requestDtoList.get(i);

            destination.setContentIdx(requestDto.getContentIdx());
            destination.setTitle(requestDto.getTitle());
            destination.setMapX(requestDto.getMapX());
            destination.setMapY(requestDto.getMapY());
            destination.setAddr1(requestDto.getAddr1());
            destination.setAddr2(requestDto.getAddr2());
            destination.setVisitDate(requestDto.getVisitDate());
            destination.setOrderNum(requestDto.getOrderNum());
        }
        planRepository.save(plan);

        return true;
    }
}
