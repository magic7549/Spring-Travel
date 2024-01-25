package com.yong.traeblue.service;

import com.yong.traeblue.config.exception.CustomException;
import com.yong.traeblue.config.exception.ErrorCode;
import com.yong.traeblue.domain.Member;
import com.yong.traeblue.domain.Plan;
import com.yong.traeblue.dto.plans.MyPlanListResponseDto;
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


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(RestDocumentationExtension.class)
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2, replace = AutoConfigureTestDatabase.Replace.ANY)
@ExtendWith(MockitoExtension.class)
class PlanServiceTest {
    @Mock
    PlanRepository planRepository;

    @Mock
    MemberRepository memberRepository;

    @InjectMocks
    PlanService planService;

    @DisplayName("계획 생성 테스트")
    @Nested
    class CreatePlanTests {
        @DisplayName("계획 생성 - 성공")
        @Test
        public void createPlanSuccess() {
            //given
            when(memberRepository.findById(any())).thenReturn(Optional.ofNullable(Member.builder()
                    .username("test")
                    .password("qwer1234")
                    .email("test@test.com")
                    .phone("01012345678")
                    .build()));

            Plan plan = Plan.builder()
                    .memberIdx(1L)
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
            Plan plan = Plan.builder()
                    .memberIdx(1L)
                    .title("FirstTravel")
                    .startDate(LocalDate.parse("2024-01-12"))
                    .endDate(LocalDate.parse("2024-01-17"))
                    .build();
            ReflectionTestUtils.setField(plan, "idx", 1L);

            List<Plan> myPlans = new ArrayList<>();
            myPlans.add(plan);

            when(planRepository.findByMemberIdx(any())).thenReturn(myPlans);

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
            List<Plan> myPlans = new ArrayList<>();

            when(planRepository.findByMemberIdx(any())).thenReturn(myPlans);

            //when
            List<MyPlanListResponseDto> findMyPlans = planService.findAllMyPlan(1L);

            //then
            assertThat(findMyPlans).isNotNull();
            assertThat(findMyPlans.size()).isEqualTo(0);
        }
    }
}