package com.yong.traeblue.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yong.traeblue.config.exception.CustomException;
import com.yong.traeblue.config.exception.ErrorCode;
import com.yong.traeblue.domain.Destination;
import com.yong.traeblue.domain.Member;
import com.yong.traeblue.domain.Plan;
import com.yong.traeblue.dto.destination.AddDestinationRequestDto;
import com.yong.traeblue.dto.destination.DestinationResponseDto;
import com.yong.traeblue.dto.destination.SaveDestinationRequestDto;
import com.yong.traeblue.dto.plans.MyPlanListResponseDto;
import com.yong.traeblue.dto.plans.PlaceResponseDto;
import com.yong.traeblue.dto.plans.PlanResponseDto;
import com.yong.traeblue.dto.plans.SearchPlaceResponseDto;
import com.yong.traeblue.repository.DestinationRepository;
import com.yong.traeblue.repository.MemberRepository;
import com.yong.traeblue.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlanService {
    private final WebClient webClient;
    private final DestinationRepository destinationRepository;
    private final PlanRepository planRepository;
    private final MemberRepository memberRepository;

    @Value("${api.tour.key}")
    private String tourApiKey;

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

    // 관광지 목록 조회
    public SearchPlaceResponseDto getPlaces(String numOfRows, String pageNo, String keyword, String areaCode, String sigunguCode) throws UnsupportedEncodingException {
        String baseUrl = "https://apis.data.go.kr/B551011/KorService1/";
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl);

        if (keyword.equals("")) {
            builder.path("areaBasedList1");
            builder
                    .queryParam("MobileOS", "ETC")
                    .queryParam("MobileApp", "Traeblue")
                    .queryParam("_type", "json")
                    .queryParam("numOfRows", numOfRows)
                    .queryParam("pageNo", pageNo)
                    .queryParam("areaCode", areaCode)
                    .queryParam("sigunguCode", sigunguCode);
        } else {
            builder.path("searchKeyword1");
            builder
                    .queryParam("MobileOS", "ETC")
                    .queryParam("MobileApp", "Traeblue")
                    .queryParam("_type", "json")
                    .queryParam("numOfRows", numOfRows)
                    .queryParam("pageNo", pageNo)
                    .queryParam("areaCode", areaCode)
                    .queryParam("sigunguCode", sigunguCode)
                    .queryParam("keyword", keyword);
        }

        String finalUrl = builder.toUriString();
        finalUrl += "&serviceKey=" + tourApiKey;

        String getData = webClient.mutate()
                .build()
                .get()
                .uri(finalUrl)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(getData);
            JsonNode itemsNode = jsonNode.path("response").path("body").path("items").path("item");

            List<PlaceResponseDto> placeResponseDtoList = new ArrayList<>();
            for (JsonNode itemNode : itemsNode) {
                PlaceResponseDto responseDto = objectMapper.treeToValue(itemNode, PlaceResponseDto.class);
                placeResponseDtoList.add(responseDto);
            }

            JsonNode totalCountNode = jsonNode.path("response").path("body").path("totalCount");
            SearchPlaceResponseDto responseDto = new SearchPlaceResponseDto();
            responseDto.setTotalCount(totalCountNode.asInt());
            responseDto.setResponseDtoList(placeResponseDtoList);

            return responseDto;
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.UNKNOWN);
        }
    }

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

    // 목적지 목록 업데이트
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
