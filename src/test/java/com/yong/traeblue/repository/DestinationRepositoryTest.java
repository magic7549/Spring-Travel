package com.yong.traeblue.repository;

import com.yong.traeblue.domain.Destination;
import com.yong.traeblue.domain.Member;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2, replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource("classpath:application.yml") //test용 properties 파일 설정
@DataJpaTest
class DestinationRepositoryTest {
    @Autowired
    DestinationRepository destinationRepository;

    @Autowired
    PlanRepository planRepository;

    @Autowired
    MemberRepository memberRepository;

    @AfterEach
    public void init() {
        destinationRepository.deleteAll();
    }

    @DisplayName("목적지 저장 테스트")
    @Test
    @Transactional
    public void saveMemberTest() {
        //given
        Member member = Member.builder()
                .username("test")
                .password("qwer1234")
                .email("test@test.com")
                .phone("01012345678")
                .build();
        ReflectionTestUtils.setField(member, "idx", 1L);
        memberRepository.save(member);

        Plan plan = Plan.builder()
                .member(member)
                .title("진도 여행")
                .startDate(LocalDate.parse("2024-01-12"))
                .endDate(LocalDate.parse("2024-01-17"))
                .build();
        ReflectionTestUtils.setField(plan, "idx", 1L);
        planRepository.save(plan);

        Destination destination = Destination.builder()
                .plan(plan)
                .contentIdx(126273)
                .title("가계해수욕장")
                .addr1("전라남도 진도군 고군면 신비의바닷길 47")
                .addr2("(고군면)")
                .mapX(126.3547412438)
                .mapY(34.4354594945)
                .visitDate(1)
                .orderNum(1)
                .build();

        //when
        Destination newDestination = destinationRepository.save(destination);

        //then
        assertThat(newDestination.getIdx()).isGreaterThan(0);
        assertThat(newDestination.getContentIdx()).isEqualTo(126273);
        assertThat(newDestination.getTitle()).isEqualTo("가계해수욕장");
        assertThat(newDestination.getAddr1()).isEqualTo("전라남도 진도군 고군면 신비의바닷길 47");
        assertThat(newDestination.getAddr2()).isEqualTo("(고군면)");
        assertThat(newDestination.getMapX()).isEqualTo(126.3547412438);
        assertThat(newDestination.getMapY()).isEqualTo(34.4354594945);
        assertThat(newDestination.getVisitDate()).isEqualTo(1);
        assertThat(newDestination.getOrderNum()).isEqualTo(1);
    }
}