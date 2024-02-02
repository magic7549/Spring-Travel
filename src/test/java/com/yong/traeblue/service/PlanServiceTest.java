package com.yong.traeblue.service;

import com.yong.traeblue.config.exception.CustomException;
import com.yong.traeblue.config.exception.ErrorCode;
import com.yong.traeblue.domain.Destination;
import com.yong.traeblue.domain.Member;
import com.yong.traeblue.domain.Plan;
import com.yong.traeblue.dto.destination.AddDestinationRequestDto;
import com.yong.traeblue.dto.plans.MyPlanListResponseDto;
import com.yong.traeblue.dto.plans.PlaceResponseDto;
import com.yong.traeblue.dto.plans.PlanResponseDto;
import com.yong.traeblue.repository.DestinationRepository;
import com.yong.traeblue.repository.MemberRepository;
import com.yong.traeblue.repository.PlanRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.http.HttpStatus;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(RestDocumentationExtension.class)
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2, replace = AutoConfigureTestDatabase.Replace.ANY)
@ExtendWith(MockitoExtension.class)
class PlanServiceTest {
    @Mock
    DestinationRepository destinationRepository;

    @Mock
    PlanRepository planRepository;

    @Mock
    MemberRepository memberRepository;

    @Mock
    WebClient webClient;

    @InjectMocks
    PlanService planService;

    @DisplayName("계획 생성 테스트")
    @Nested
    class CreatePlanTests {
        @DisplayName("계획 생성 - 성공")
        @Test
        public void createPlanSuccess() {
            //given
            Member member = Member.builder()
                    .username("test")
                    .password("qwer1234")
                    .email("test@test.com")
                    .phone("01012345678")
                    .build();

            when(memberRepository.findById(any())).thenReturn(Optional.ofNullable(member));

            Plan plan = Plan.builder()
                    .member(member)
                    .title("FirstTravel")
                    .startDate(LocalDate.parse("2024-01-12"))
                    .endDate(LocalDate.parse("2024-01-17"))
                    .build();
            ReflectionTestUtils.setField(plan, "idx", 1L);
            when(planRepository.save(any())).thenReturn(plan);

            //when
            Long planIdx = planService.createPlan(1L, "FirstTravel", "2024-01-12", "2024-01-17");

            //then
            assertThat(planIdx).isNotNull();
            assertThat(planIdx).isGreaterThan(0);
        }

        @DisplayName("계획 생성 - 실패")
        @Test
        public void createPlanFail() {
            //given
            when(memberRepository.findById(any())).thenThrow(new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_EXISTED_MEMBER));

            //when
            try {
                Long planIdx = planService.createPlan(1L, "FirstTravel", "2024-01-12", "2024-01-17");
            } catch (CustomException e) {
                //then
                assertThat(e.getErrorCode()).isEqualTo(ErrorCode.NOT_EXISTED_MEMBER);
            }
        }
    }

    @DisplayName("내 계획 목록 조회 테스트")
    @Nested
    class FindMyPlansTests {
        @DisplayName("목록 조회 성공")
        @Test
        public void findPlansSuccess() {
            //given
            Member member = Member.builder()
                    .username("test")
                    .password("qwer1234")
                    .email("test@test.com")
                    .phone("01012345678")
                    .build();
            ReflectionTestUtils.setField(member, "idx", 1L);

            when(memberRepository.findById(any())).thenReturn(Optional.of(member));

            Plan plan = Plan.builder()
                    .member(member)
                    .title("FirstTravel")
                    .startDate(LocalDate.parse("2024-01-12"))
                    .endDate(LocalDate.parse("2024-01-17"))
                    .build();
            ReflectionTestUtils.setField(plan, "idx", 1L);

            List<Plan> myPlans = new ArrayList<>();
            myPlans.add(plan);
            when(planRepository.findByMember(member)).thenReturn(myPlans);

            //when
            List<MyPlanListResponseDto> findMyPlans = planService.findAllMyPlan(1L);

            //then
            assertThat(findMyPlans).isNotNull();
            assertThat(findMyPlans.size()).isGreaterThan(0);
        }

        @DisplayName("나의 계획이 없음")
        @Test
        public void myPlansIsEmpty() {
            //given
            Member member = Member.builder()
                    .username("test")
                    .password("qwer1234")
                    .email("test@test.com")
                    .phone("01012345678")
                    .build();
            ReflectionTestUtils.setField(member, "idx", 1L);

            when(memberRepository.findById(any())).thenReturn(Optional.of(member));

            List<Plan> myPlans = new ArrayList<>();
            when(planRepository.findByMember(any())).thenReturn(myPlans);

            //when
            List<MyPlanListResponseDto> findMyPlans = planService.findAllMyPlan(1L);

            //then
            assertThat(findMyPlans).isNotNull();
            assertThat(findMyPlans.size()).isEqualTo(0);
        }
    }

    @DisplayName("계획 조회 테스트")
    @Nested
    class DetailPlanTests {
        @DisplayName("목록 조회 성공 - 빈 계획 일 때")
        @Test
        public void findPlansDestinationIsNullSuccess() {
            //given
            Member member = Member.builder()
                    .username("test")
                    .password("qwer1234")
                    .email("test@test.com")
                    .phone("01012345678")
                    .build();
            ReflectionTestUtils.setField(member, "idx", 1L);

            Plan plan = Plan.builder()
                    .member(member)
                    .title("FirstTravel")
                    .startDate(LocalDate.parse("2024-01-12"))
                    .endDate(LocalDate.parse("2024-01-17"))
                    .build();
            ReflectionTestUtils.setField(plan, "idx", 1L);

            when(planRepository.findById(1L)).thenReturn(Optional.of(plan));

            //when
            PlanResponseDto findMyPlan = planService.findById(1L);

            //then
            assertThat(findMyPlan).isNotNull();
            assertThat(findMyPlan.getTitle()).isEqualTo("FirstTravel");
            assertThat(findMyPlan.getDestinations()).isNull();
        }

        @DisplayName("목록 조회 성공")
        @Test
        public void findPlansSuccess() {
            //given
            Member member = Member.builder()
                    .username("test")
                    .password("qwer1234")
                    .email("test@test.com")
                    .phone("01012345678")
                    .build();
            ReflectionTestUtils.setField(member, "idx", 1L);

            List<Destination> destinations = new ArrayList<>();
            destinations.add(Destination.builder()
                    .contentIdx(126273)
                    .title("가계해수욕장")
                    .addr1("전라남도 진도군 고군면 신비의바닷길 47")
                    .addr2("(고군면)")
                    .mapX(126.3547412438)
                    .mapY(34.4354594945)
                    .visitDate(1)
                    .orderNum(1)
                    .build());

            Plan plan = Plan.builder()
                    .member(member)
                    .title("FirstTravel")
                    .startDate(LocalDate.parse("2024-01-12"))
                    .endDate(LocalDate.parse("2024-01-17"))
                    .build();
            ReflectionTestUtils.setField(plan, "idx", 1L);
            ReflectionTestUtils.setField(plan, "destinations", destinations);

            when(planRepository.findById(1L)).thenReturn(Optional.of(plan));

            //when
            PlanResponseDto findMyPlan = planService.findById(1L);

            //then
            assertThat(findMyPlan).isNotNull();
            assertThat(findMyPlan.getTitle()).isEqualTo("FirstTravel");
            assertThat(findMyPlan.getDestinations()).isNotNull();
            assertThat(findMyPlan.getDestinations().iterator().next().getTitle()).isEqualTo("가계해수욕장");
        }

        @DisplayName("목록 조회 실패")
        @Test
        public void findPlansFail() {
            //given
            Member member = Member.builder()
                    .username("test")
                    .password("qwer1234")
                    .email("test@test.com")
                    .phone("01012345678")
                    .build();
            ReflectionTestUtils.setField(member, "idx", 1L);

            Plan plan = Plan.builder()
                    .member(member)
                    .title("FirstTravel")
                    .startDate(LocalDate.parse("2024-01-12"))
                    .endDate(LocalDate.parse("2024-01-17"))
                    .build();
            ReflectionTestUtils.setField(plan, "idx", 1L);

            when(planRepository.findById(1L)).thenThrow(new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_EXISTED_PLAN));

            //when
            try {
                PlanResponseDto findMyPlan = planService.findById(1L);
            } catch (CustomException e) {
                //then
                assertThat(e.getErrorCode()).isEqualTo(ErrorCode.NOT_EXISTED_PLAN);
            }
        }
    }

    @DisplayName("목적지 저장 테스트")
    @Nested
    class SaveDestinationTests {
        @DisplayName("목적지 저장 성공")
        @Test
        public void saveDestinationSuccess() {
            //given
            Member member = Member.builder()
                    .username("test")
                    .password("qwer1234")
                    .email("test@test.com")
                    .phone("01012345678")
                    .build();
            ReflectionTestUtils.setField(member, "idx", 1L);

            Plan plan = Plan.builder()
                    .member(member)
                    .title("FirstTravel")
                    .startDate(LocalDate.parse("2024-01-12"))
                    .endDate(LocalDate.parse("2024-01-17"))
                    .build();
            ReflectionTestUtils.setField(plan, "idx", 1L);

            when(planRepository.findById(any())).thenReturn(Optional.ofNullable(plan));

            AddDestinationRequestDto requestDto = new AddDestinationRequestDto();
            requestDto.setTitle("가계해수욕장");
            requestDto.setAddr1("전라남도 진도군 고군면 신비의바닷길 47");
            requestDto.setAddr2("(고군면)");
            requestDto.setMapX(126.3547412438);
            requestDto.setMapY(34.4354594945);
            requestDto.setVisitDate(1);

            //when
            boolean isSuccess = planService.addDestination(1L, requestDto);

            //then
            assertThat(isSuccess).isTrue();
        }

        @DisplayName("목적지 저장 실패 - 계획이 존재 X")
        @Test
        public void saveDestinationFail() {
            //given
            Member member = Member.builder()
                    .username("test")
                    .password("qwer1234")
                    .email("test@test.com")
                    .phone("01012345678")
                    .build();
            ReflectionTestUtils.setField(member, "idx", 1L);

            Plan plan = Plan.builder()
                    .member(member)
                    .title("FirstTravel")
                    .startDate(LocalDate.parse("2024-01-12"))
                    .endDate(LocalDate.parse("2024-01-17"))
                    .build();
            ReflectionTestUtils.setField(plan, "idx", 1L);

            when(planRepository.findById(any())).thenThrow(new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_EXISTED_PLAN));

            AddDestinationRequestDto requestDto = new AddDestinationRequestDto();
            requestDto.setTitle("가계해수욕장");
            requestDto.setAddr1("전라남도 진도군 고군면 신비의바닷길 47");
            requestDto.setAddr2("(고군면)");
            requestDto.setMapX(126.3547412438);
            requestDto.setMapY(34.4354594945);
            requestDto.setVisitDate(1);

            //when
            try {
                boolean isSuccess = planService.addDestination(1L, requestDto);
            } catch (CustomException e) {
                //then
                assertThat(e.getErrorCode()).isEqualTo(ErrorCode.NOT_EXISTED_PLAN);
            }
        }
    }
}