package com.yong.traeblue.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yong.traeblue.config.jwt.JWTUtil;
import com.yong.traeblue.domain.Destination;
import com.yong.traeblue.domain.Member;
import com.yong.traeblue.domain.Plan;
import com.yong.traeblue.dto.plans.CreatePlanRequestDto;
import com.yong.traeblue.repository.MemberRepository;
import com.yong.traeblue.repository.PlanRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.operation.preprocess.Preprocessors;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.modifyUris;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureRestDocs
@AutoConfigureMockMvc
@ExtendWith(RestDocumentationExtension.class)
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2, replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource("classpath:application.yml") //test용 properties 파일 설정
public class PlanTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private PlanRepository planRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @BeforeEach
    public void setUp(RestDocumentationContextProvider restDocumentation) {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .addFilter(new CharacterEncodingFilter("UTF-8", true))
                .apply(documentationConfiguration(restDocumentation)
                        .operationPreprocessors()
                        .withRequestDefaults(
                                modifyUris().scheme("https").host("docs.api.com").removePort(), prettyPrint())
                        .withResponseDefaults(prettyPrint())
                )
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @BeforeEach
    public void init() {
        planRepository.deleteAll();
        memberRepository.deleteAll();
        memberRepository.save(Member.builder()
                .username("test")
                .password(bCryptPasswordEncoder.encode("123123123a"))
                .email("test@test.com")
                .phone("01012345678")
                .build());
    }

    @DisplayName("계획 생성 테스트")
    @Nested
    class CreatePlanTests {
        @DisplayName("계획 생성 성공")
        @WithMockUser
        @Test
        public void createPlanSuccess() throws Exception {
            //given
            CreatePlanRequestDto requestDto = new CreatePlanRequestDto();
            requestDto.setTitle("First Travel");
            requestDto.setStartDate("2024-01-12");
            requestDto.setEndDate("2024-01-17");
            String body = objectMapper.writeValueAsString(requestDto);

            Long memberIdx = memberRepository.findByUsername("test").get().getIdx();

            String accessToken = jwtUtil.createAccess(memberIdx, "test", "ROLE_USER");
            Cookie accessCookie = new Cookie("access", accessToken);

            //when
            ResultActions result = mockMvc.perform(post("/api/v1/plans")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)
                    .cookie(accessCookie));

            //then
            result
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.planIdx").isNotEmpty())
                    .andDo(document("plans/create",
                            Preprocessors.preprocessRequest(prettyPrint()),
                            Preprocessors.preprocessResponse(prettyPrint()),
                            requestFields(
                                    fieldWithPath("title").description("제목"),
                                    fieldWithPath("startDate").description("시작일"),
                                    fieldWithPath("endDate").description("종료일")
                            ),
                            responseFields(
                                    fieldWithPath("planIdx").description("생성된 계획 인덱스 값")
                            )));
        }

        @DisplayName("계획 생성 실패")
        @WithMockUser
        @Test
        public void createPlanFail() throws Exception {
            //given
            CreatePlanRequestDto requestDto = new CreatePlanRequestDto();
            requestDto.setTitle("First Travel");
            requestDto.setStartDate("2024-01-12");
            requestDto.setEndDate("2024-01-17");
            String body = objectMapper.writeValueAsString(requestDto);

            Long memberIdx = memberRepository.findByUsername("test").get().getIdx() + 1;

            String accessToken = jwtUtil.createAccess(memberIdx, "test123", "ROLE_USER");
            Cookie accessCookie = new Cookie("access", accessToken);

            //when
            ResultActions result = mockMvc.perform(post("/api/v1/plans")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)
                    .cookie(accessCookie));

            //then
            result
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("NOT_EXISTED_MEMBER"));;
        }
    }

    @DisplayName("내 계획 목록 조회 테스트")
    @Nested
    class FindMyPlansTests {
        @DisplayName("내 계획 목록 조회 성공")
        @WithMockUser
        @Test
        public void findMyPlansSuccess() throws Exception {
            //given
            Optional<Member> member = memberRepository.findByUsername("test");
            planRepository.save(Plan.builder()
                    .member(member.get())
                    .title("First Travel")
                    .startDate(LocalDate.parse("2024-01-12"))
                    .endDate(LocalDate.parse("2024-01-17"))
                    .build());


            String accessToken = jwtUtil.createAccess(member.get().getIdx(), "test", "ROLE_USER");
            Cookie accessCookie = new Cookie("access", accessToken);

            //when
            ResultActions result = mockMvc.perform(get("/api/v1/plans")
                    .cookie(accessCookie));

            //then
            result
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].idx").isNotEmpty())
                    .andExpect(jsonPath("$[0].title").isNotEmpty())
                    .andExpect(jsonPath("$[0].startDate").isNotEmpty())
                    .andExpect(jsonPath("$[0].endDate").isNotEmpty())
                    .andDo(document("plans/find/list",
                            Preprocessors.preprocessRequest(prettyPrint()),
                            Preprocessors.preprocessResponse(prettyPrint()),
                            responseFields(
                                    fieldWithPath("[].idx").description("생성된 계획 인덱스 값"),
                                    fieldWithPath("[].title").description("계획 제목"),
                                    fieldWithPath("[].startDate").description("계획 시작일"),
                                    fieldWithPath("[].endDate").description("계획 종료일")
                            )));
        }

        @DisplayName("빈 목록 조회")
        @WithMockUser
        @Test
        public void findMyPlansIsEmpty() throws Exception {
            //given
            Long memberIdx = memberRepository.findByUsername("test").get().getIdx();

            String accessToken = jwtUtil.createAccess(memberIdx, "test", "ROLE_USER");
            Cookie accessCookie = new Cookie("access", accessToken);

            //when
            ResultActions result = mockMvc.perform(get("/api/v1/plans")
                    .cookie(accessCookie));

            //then
            result
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isEmpty());
        }
    }

    @DisplayName("계획 상세 조회 테스트")
    @Nested
    class GetPlanDetailTests {
        @DisplayName("계획 상세 조회 성공")
        @WithMockUser
        @Test
        public void getPlanDetailSuccess() throws Exception {
            //given
            Optional<Member> member = memberRepository.findByUsername("test");
            Plan plan = Plan.builder()
                    .member(member.get())
                    .title("First Travel")
                    .startDate(LocalDate.parse("2024-01-12"))
                    .endDate(LocalDate.parse("2024-01-17"))
                    .build();

            List<Destination> destinations = new ArrayList<>();
            destinations.add(Destination.builder()
                    .plan(plan)
                    .contentIdx(126273)
                    .title("가계해수욕장")
                    .addr1("전라남도 진도군 고군면 신비의바닷길 47")
                    .addr2("(고군면)")
                    .mapX(126.3547412438)
                    .mapY(34.4354594945)
                    .visitDate(1)
                    .orderNum(1)
                    .build());
            destinations.add(Destination.builder()
                    .plan(plan)
                    .contentIdx(2019720)
                    .title("가고파 꼬부랑길 벽화마을")
                    .addr1("경상남도 창원시 마산합포구 성호서7길 15-8")
                    .addr2("")
                    .mapX(128.5696552845)
                    .mapY(35.2077664004)
                    .visitDate(1)
                    .orderNum(2)
                    .build());

            ReflectionTestUtils.setField(plan, "idx", 1L);
            ReflectionTestUtils.setField(plan, "destinations", destinations);

            Plan getPlan = planRepository.save(plan);

            String accessToken = jwtUtil.createAccess(member.get().getIdx(), "test", "ROLE_USER");
            Cookie accessCookie = new Cookie("access", accessToken);

            //when
            ResultActions result = mockMvc.perform(get("/api/v1/plans/{idx}", getPlan.getIdx())
                    .cookie(accessCookie));

            //then
            result
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("First Travel"))
                    .andExpect(jsonPath("$.startDate").value("2024-01-12"))
                    .andExpect(jsonPath("$.endDate").value("2024-01-17"))
                    .andExpect(jsonPath("$.destinations[0].title").value("가계해수욕장"))
                    .andExpect(jsonPath("$.destinations[0].addr1").value("전라남도 진도군 고군면 신비의바닷길 47"))
                    .andExpect(jsonPath("$.destinations[0].addr2").value("(고군면)"))
                    .andExpect(jsonPath("$.destinations[0].mapX").value(126.3547412438))
                    .andExpect(jsonPath("$.destinations[0].mapY").value(34.4354594945))
                    .andExpect(jsonPath("$.destinations[0].visitDate").value(1))
                    .andExpect(jsonPath("$.destinations[0].orderNum").value(1))
                    .andDo(document("plans/find/detail",
                            Preprocessors.preprocessRequest(prettyPrint()),
                            Preprocessors.preprocessResponse(prettyPrint()),
                            responseFields(
                                    fieldWithPath("title").description("계획 제목"),
                                    fieldWithPath("startDate").description("계획 시작일"),
                                    fieldWithPath("endDate").description("계획 종료일"),
                                    fieldWithPath("travelDuration").description("여행 기간"),
                                    fieldWithPath("destinations").description("목적지 목록").type(JsonFieldType.ARRAY),
                                    fieldWithPath("destinations[].content_idx").description("콘텐츠 ID"),
                                    fieldWithPath("destinations[].title").description("관광지 이름"),
                                    fieldWithPath("destinations[].addr1").description("상세 주소1"),
                                    fieldWithPath("destinations[].addr2").description("상세 주소2"),
                                    fieldWithPath("destinations[].mapX").description("지도 X 좌표"),
                                    fieldWithPath("destinations[].mapY").description("지도 Y 좌표"),
                                    fieldWithPath("destinations[].visitDate").description("방문 날짜"),
                                    fieldWithPath("destinations[].orderNum").description("방문 순서")
                            )
                    ));
        }

        @DisplayName("계획 상세 조회 실패")
        @WithMockUser
        @Test
        public void getPlanDetailFail() throws Exception {
            //given
            Optional<Member> member = memberRepository.findByUsername("test");
            Plan plan = Plan.builder()
                    .member(member.get())
                    .title("First Travel")
                    .startDate(LocalDate.parse("2024-01-12"))
                    .endDate(LocalDate.parse("2024-01-17"))
                    .build();

            List<Destination> destinations = new ArrayList<>();
            destinations.add(Destination.builder()
                    .plan(plan)
                    .contentIdx(126273)
                    .title("가계해수욕장")
                    .addr1("전라남도 진도군 고군면 신비의바닷길 47")
                    .addr2("(고군면)")
                    .mapX(126.3547412438)
                    .mapY(34.4354594945)
                    .visitDate(1)
                    .orderNum(1)
                    .build());
            destinations.add(Destination.builder()
                    .plan(plan)
                    .contentIdx(2019720)
                    .title("가고파 꼬부랑길 벽화마을")
                    .addr1("경상남도 창원시 마산합포구 성호서7길 15-8")
                    .addr2("")
                    .mapX(128.5696552845)
                    .mapY(35.2077664004)
                    .visitDate(1)
                    .orderNum(2)
                    .build());

            ReflectionTestUtils.setField(plan, "idx", 1L);
            ReflectionTestUtils.setField(plan, "destinations", destinations);

            Plan getPlan = planRepository.save(plan);

            String accessToken = jwtUtil.createAccess(member.get().getIdx(), "test", "ROLE_USER");
            Cookie accessCookie = new Cookie("access", accessToken);

            //when
            ResultActions result = mockMvc.perform(get("/api/v1/plans/{idx}", getPlan.getIdx() + 1)
                    .cookie(accessCookie));

            //then
            result
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("NOT_EXISTED_PLAN"))
                    .andExpect(jsonPath("$.msg").value("존재하지 않는 계획입니다."));
        }
    }
}
