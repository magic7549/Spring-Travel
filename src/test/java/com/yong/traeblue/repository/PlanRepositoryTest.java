package com.yong.traeblue.repository;

import com.yong.traeblue.domain.Plan;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2, replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource("classpath:application.yml") //test용 properties 파일 설정
@DataJpaTest
class PlanRepositoryTest {
    @Autowired
    PlanRepository planRepository;

    @AfterEach
    public void init() {
        planRepository.deleteAll();
    }

    @DisplayName("계획 생성 테스트")
    @Test
    @Transactional
    public void createPlan() {
        //given
        Plan plan = Plan.builder()
                .memberIdx(1L)
                .title("test plan")
                .startDate(LocalDate.of(2000, 1, 1))
                .endDate(LocalDate.of(2000, 1, 5))
                .build();

        //when
        Plan newPlan = planRepository.save(plan);

        //then
        assertThat(newPlan.getIdx()).isGreaterThan(0);
        assertThat(newPlan.getMemberIdx()).isEqualTo(1L);
        assertThat(newPlan.getTitle()).isEqualTo("test plan");
    }

    @DisplayName("내 계획 목록 조회 테스트")
    @Test
    @Transactional
    public void findMyPlans() {
        //given
        Plan plan = Plan.builder()
                .memberIdx(1L)
                .title("test plan")
                .startDate(LocalDate.of(2000, 1, 1))
                .endDate(LocalDate.of(2000, 1, 5))
                .build();

        Plan plan2 = Plan.builder()
                .memberIdx(1L)
                .title("test2 plan")
                .startDate(LocalDate.of(2000, 7, 21))
                .endDate(LocalDate.of(2000, 7, 27))
                .build();

        planRepository.save(plan);
        planRepository.save(plan2);

        //when
        List<Plan> myPlans = planRepository.findByMemberIdx(1L);


        //then
        assertThat(myPlans.size()).isEqualTo(2);
    }
}