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

import java.util.Date;

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
                .startDate(new Date(2000, 1, 1))
                .endDate(new Date(2000, 1, 5))
                .build();

        //when
        Plan newPlan = planRepository.save(plan);

        //then
        assertThat(newPlan.getIdx()).isGreaterThan(0);
        assertThat(newPlan.getMemberIdx()).isEqualTo(1L);
        assertThat(newPlan.getTitle()).isEqualTo("test plan");
    }
}