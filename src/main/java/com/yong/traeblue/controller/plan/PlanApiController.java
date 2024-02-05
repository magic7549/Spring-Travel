package com.yong.traeblue.controller.plan;

import com.yong.traeblue.config.jwt.JWTUtil;
import com.yong.traeblue.dto.destination.AddDestinationRequestDto;
import com.yong.traeblue.dto.destination.DeleteDestinationRequestDto;
import com.yong.traeblue.dto.destination.SaveDestinationRequestDto;
import com.yong.traeblue.dto.plans.*;
import com.yong.traeblue.service.PlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
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
    public ResponseEntity<PlanResponseDto> getPlanDetail(@PathVariable(name = "idx") Long planIdx) {
        return ResponseEntity.ok().body(planService.findById(planIdx));
    }

    // 계획 이름 변경
    @PatchMapping("/plans/{idx}/title")
    public ResponseEntity<Map<String, Boolean>> setPlanTitle(@PathVariable(name = "idx") Long planIdx, @RequestBody ChangePlanTitleRequestDto requestDto) {
        return ResponseEntity.ok().body(Collections.singletonMap("isSuccess", planService.setPlanTitle(planIdx, requestDto.getTitle())));
    }

    // 관광지 목록 조회
    @GetMapping("/places")
    public ResponseEntity<SearchPlaceResponseDto> getPlaces(
            @RequestParam(name = "numOfRows", defaultValue = "10") String numOfRows,
            @RequestParam(name = "pageNo", defaultValue = "1") String pageNo,
            @RequestParam(name = "keyword", defaultValue = "") String keyword,
            @RequestParam(name = "areaCode", defaultValue = "") String areaCode,
            @RequestParam(name = "sigunguCode", defaultValue = "") String sigunguCode) throws UnsupportedEncodingException {
        SearchPlaceResponseDto responseDto = planService.getPlaces(numOfRows, pageNo, keyword, areaCode, sigunguCode);

        return ResponseEntity.ok().body(responseDto);
    }

    // 관광지 디테일 조회
    @GetMapping("/places/{contentId}")
    public ResponseEntity<PlaceDetailsResponseDto> getPlaceDetails(@PathVariable(name = "contentId") Long contentId) throws UnsupportedEncodingException {
        PlaceDetailsResponseDto responseDto = planService.getPlaceDetails(contentId.toString());

        return ResponseEntity.ok().body(responseDto);
    }

    // 목적지 추가
    @PostMapping("/destinations/{idx}")
    public ResponseEntity<Map<String, Boolean>> addDestination(@PathVariable(name = "idx") Long planIdx, @RequestBody AddDestinationRequestDto requestDto) {
        boolean isSuccess = planService.addDestination(planIdx, requestDto);

        return ResponseEntity.ok().body(Collections.singletonMap("isSuccess", isSuccess));
    }

    // 목적지 목록 업데이트
    @PutMapping("/destinations/{idx}")
    public ResponseEntity<Map<String, Boolean>> updateDestinations(@PathVariable(name = "idx") Long planIdx, @RequestBody List<SaveDestinationRequestDto> requestDtoList) {
        boolean isSuccess = planService.updateDestinations(planIdx, requestDtoList);

        return ResponseEntity.ok().body(Collections.singletonMap("isSuccess", isSuccess));
    }

    // 목적지 삭제
    @DeleteMapping("/destinations/{idx}")
    public ResponseEntity<Map<String, Boolean>> deleteDestination(@PathVariable(name = "idx") Long planIdx, @RequestBody DeleteDestinationRequestDto requestDto) {
        boolean isSuccess = planService.deleteDestination(planIdx, requestDto);

        return ResponseEntity.ok().body(Collections.singletonMap("isSuccess", isSuccess));
    }
}
