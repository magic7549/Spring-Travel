package com.yong.traeblue.service;

import com.yong.traeblue.config.exception.CustomException;
import com.yong.traeblue.config.exception.ErrorCode;
import com.yong.traeblue.domain.Member;
import com.yong.traeblue.domain.Plan;
import com.yong.traeblue.dto.destination.AddDestinationRequestDto;
import com.yong.traeblue.repository.DestinationRepository;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(RestDocumentationExtension.class)
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2, replace = AutoConfigureTestDatabase.Replace.ANY)
@ExtendWith(MockitoExtension.class)
class DestinationServiceTest {
    @Mock
    DestinationRepository destinationRepository;

    @Mock
    PlanRepository planRepository;

    @InjectMocks
    DestinationService destinationService;

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
            requestDto.setPlanIdx(1L);
            requestDto.setTitle("가계해수욕장");
            requestDto.setAddr1("전라남도 진도군 고군면 신비의바닷길 47");
            requestDto.setAddr2("(고군면)");
            requestDto.setMapX(126.3547412438);
            requestDto.setMapY(34.4354594945);
            requestDto.setVisitDate(1);
            requestDto.setOrderNum(1);

            //when
            boolean isSuccess = destinationService.save(requestDto);

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
            requestDto.setPlanIdx(1L);
            requestDto.setTitle("가계해수욕장");
            requestDto.setAddr1("전라남도 진도군 고군면 신비의바닷길 47");
            requestDto.setAddr2("(고군면)");
            requestDto.setMapX(126.3547412438);
            requestDto.setMapY(34.4354594945);
            requestDto.setVisitDate(1);
            requestDto.setOrderNum(1);

            //when
            try {
                boolean isSuccess = destinationService.save(requestDto);
            } catch (CustomException e) {
                //then
                assertThat(e.getErrorCode()).isEqualTo(ErrorCode.NOT_EXISTED_PLAN);
            }
        }
    }
}