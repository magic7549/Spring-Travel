package com.yong.traeblue.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yong.traeblue.config.jwt.JWTUtil;
import com.yong.traeblue.domain.Destination;
import com.yong.traeblue.domain.Member;
import com.yong.traeblue.domain.Plan;
import com.yong.traeblue.dto.destination.AddDestinationRequestDto;
import com.yong.traeblue.dto.destination.DeleteDestinationRequestDto;
import com.yong.traeblue.dto.destination.SaveDestinationRequestDto;
import com.yong.traeblue.dto.plans.ChangePlanTitleRequestDto;
import com.yong.traeblue.dto.plans.CreatePlanRequestDto;
import com.yong.traeblue.dto.plans.SearchPlaceRequestDto;
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
import static org.springframework.restdocs.request.RequestDocumentation.*;
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
        memberRepository.deleteAll();
        planRepository.deleteAll();

        Member member = memberRepository.save(Member.builder()
                .username("test")
                .password(bCryptPasswordEncoder.encode("123123123a"))
                .email("test@test.com")
                .phone("01012345678")
                .build());

        Plan plan = planRepository.save(Plan.builder()
                .member(member)
                .title("Test Travel")
                .startDate(LocalDate.parse("2020-01-01"))
                .endDate(LocalDate.parse("2020-01-04"))
                .build());

        Destination destination1 = Destination.builder()
                .plan(plan)
                .contentIdx(2789460)
                .title("가덕도대항인공동굴")
                .addr1("부산광역시 강서구 대항동 393-9")
                .addr2("(대항동)")
                .mapX(128.8274685924)
                .mapY(35.0133095493)
                .visitDate(1)
                .orderNum(1)
                .build();
        Destination destination2 = Destination.builder()
                .plan(plan)
                .contentIdx(129156)
                .title("가덕도 등대")
                .addr1("부산광역시 강서구 외양포로 10")
                .addr2("")
                .mapX(128.8295937487)
                .mapY(35.0006471157)
                .visitDate(1)
                .orderNum(2)
                .build();
        List<Destination> destinations = new ArrayList<>();
        destinations.add(destination1);
        destinations.add(destination2);

        plan.setDestinations(destinations);
        planRepository.save(plan);
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
            Long memberIdx = memberRepository.save(Member.builder()
                    .username("new member")
                    .password(bCryptPasswordEncoder.encode("123123123a"))
                    .email("newEmail@email.com")
                    .phone("01012345678")
                    .build()
            ).getIdx();

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

    @DisplayName("계획 이름 변경 테스트")
    @Nested
    class SetPlanTitleTests {
        @DisplayName("이름 변경 성공")
        @WithMockUser
        @Test
        public void setPlanTitleSuccess() throws Exception {
            //given
            Optional<Member> member = memberRepository.findByUsername("test");
            Plan plan = planRepository.findByMember(member.get()).get(0);

            ChangePlanTitleRequestDto requestDto = new ChangePlanTitleRequestDto();
            requestDto.setTitle("Change Title");
            String body = objectMapper.writeValueAsString(requestDto);

            String accessToken = jwtUtil.createAccess(member.get().getIdx(), "test", "ROLE_USER");
            Cookie accessCookie = new Cookie("access", accessToken);

            //when
            ResultActions result = mockMvc.perform(patch("/api/v1/plans/{idx}/title", plan.getIdx())
                    .cookie(accessCookie)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            //then
            result
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("isSuccess").value(true))
                    .andDo(document("plans/change/title",
                            Preprocessors.preprocessRequest(prettyPrint()),
                            Preprocessors.preprocessResponse(prettyPrint()),
                            pathParameters(
                                    parameterWithName("idx").description("계획 idx")
                            ),
                            requestFields(
                                    fieldWithPath("title").description("변경할 제목")
                            ),
                            responseFields(
                                    fieldWithPath("isSuccess").description("성공 여부")
                            )
                    ));
        }
    }

    @DisplayName("관광지 목록 조회 테스트")
    @Nested
    class GetPlaceListTests {
        @DisplayName("조회 성공")
        @WithMockUser
        @Test
        public void GetPlaceListSuccess() throws Exception {
            //given
            Optional<Member> member = memberRepository.findByUsername("test");
            String accessToken = jwtUtil.createAccess(member.get().getIdx(), "test", "ROLE_USER");
            Cookie accessCookie = new Cookie("access", accessToken);

            //when
            ResultActions result = mockMvc.perform(get("/api/v1/places")
                    .param("numOfRows", "2")
                    .param("pageNo", "1")
                    .param("keyword", "")
                    .param("areaCode", "")
                    .param("sigunguCode", "")
                    .cookie(accessCookie));

            //then
            result
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalCount").isNumber())
                    .andDo(document("place/get/list",
                            Preprocessors.preprocessRequest(prettyPrint()),
                            Preprocessors.preprocessResponse(prettyPrint()),
                            queryParameters(
                                    parameterWithName("numOfRows").description("한 페이지 결과 수"),
                                    parameterWithName("pageNo").description("페이지 번호"),
                                    parameterWithName("keyword").description("검색어"),
                                    parameterWithName("areaCode").description("지역 코드"),
                                    parameterWithName("sigunguCode").description("시군구 코드")
                            ),
                            responseFields(
                                    fieldWithPath("totalCount").description("검색 결과 개수"),
                                    fieldWithPath("responseDtoList.[].addr1").description("주소 1"),
                                    fieldWithPath("responseDtoList.[].addr2").description("주소 2"),
                                    fieldWithPath("responseDtoList.[].areacode").description("지역 코드"),
                                    fieldWithPath("responseDtoList.[].booktour").description("교과서 속 여행지 여부"),
                                    fieldWithPath("responseDtoList.[].cat1").description("대분류 코드"),
                                    fieldWithPath("responseDtoList.[].cat2").description("중분류 코드"),
                                    fieldWithPath("responseDtoList.[].cat3").description("소분류 코드"),
                                    fieldWithPath("responseDtoList.[].contentid").description("콘텐츠 ID"),
                                    fieldWithPath("responseDtoList.[].contenttypeid").description("콘텐츠 타입 ID"),
                                    fieldWithPath("responseDtoList.[].createdtime").description("등록 일시"),
                                    fieldWithPath("responseDtoList.[].firstimage").description("대표 이미지 URL"),
                                    fieldWithPath("responseDtoList.[].firstimage2").description("대표 이미지2 URL"),
                                    fieldWithPath("responseDtoList.[].cpyrhtDivCd").description("저작권 유형"),
                                    fieldWithPath("responseDtoList.[].mapx").description("지도 X 좌표"),
                                    fieldWithPath("responseDtoList.[].mapy").description("지도 Y 좌표"),
                                    fieldWithPath("responseDtoList.[].mlevel").description("맵 레벨"),
                                    fieldWithPath("responseDtoList.[].modifiedtime").description("수정 일시"),
                                    fieldWithPath("responseDtoList.[].sigungucode").description("시군구 코드"),
                                    fieldWithPath("responseDtoList.[].tel").description("전화번호"),
                                    fieldWithPath("responseDtoList.[].title").description("제목"),
                                    fieldWithPath("responseDtoList.[].zipcode").description("우편번호")
                            )));
        }
    }

    @DisplayName("관광지 디테일 조회 테스트")
    @Nested
    class GetPlaceDetailsTests {
        @DisplayName("조회 성공")
        @WithMockUser
        @Test
        public void GetPlaceDetailsSuccess() throws Exception {
            //given
            Optional<Member> member = memberRepository.findByUsername("test");
            String accessToken = jwtUtil.createAccess(member.get().getIdx(), "test", "ROLE_USER");
            Cookie accessCookie = new Cookie("access", accessToken);

            //when
            ResultActions result = mockMvc.perform(get("/api/v1/places/{contentId}", 126273)
                    .cookie(accessCookie));

            //then
            result
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("가계해수욕장"))
                    .andDo(document("place/get/detail",
                            Preprocessors.preprocessRequest(prettyPrint()),
                            Preprocessors.preprocessResponse(prettyPrint()),
                            pathParameters(
                                    parameterWithName("contentId").description("콘텐츠 id")
                            ),
                            responseFields(
                                    fieldWithPath("title").description("관광지 이름"),
                                    fieldWithPath("firstimage").description("이미지 1"),
                                    fieldWithPath("firstimage2").description("이미지 2"),
                                    fieldWithPath("addr1").description("주소 1"),
                                    fieldWithPath("addr2").description("주소 2"),
                                    fieldWithPath("overview").description("개요"),
                                    fieldWithPath("contentid").description("콘텐츠 id"),
                                    fieldWithPath("contenttypeid").description("콘텐츠 타입 id"),
                                    fieldWithPath("createdtime").description("생성 시간"),
                                    fieldWithPath("modifiedtime").description("수정 시간"),
                                    fieldWithPath("tel").description("전화번호"),
                                    fieldWithPath("telname").description("전화번호명"),
                                    fieldWithPath("homepage").description("홈페이지"),
                                    fieldWithPath("booktour").description("교과서 속 여행지 여부"),
                                    fieldWithPath("cpyrhtDivCd").description("저작권 유형"),
                                    fieldWithPath("zipcode").description("우편 번호")
                            )));
        }
    }

    @DisplayName("목적지 리스트 업데이트 테스트")
    @Nested
    class UpdateDestinationsTests {
        @DisplayName("업데이트 성공")
        @WithMockUser
        @Test
        public void updateDestinationsSuccess() throws Exception {
            //given
            Optional<Member> member = memberRepository.findByUsername("test");
            List<Plan> plan = planRepository.findByMember(member.get());

            SaveDestinationRequestDto requestDto1 = new SaveDestinationRequestDto();
            requestDto1.setContentIdx(129156);
            requestDto1.setTitle("가덕도 등대");
            requestDto1.setAddr1("부산광역시 강서구 대항동 393-9");
            requestDto1.setAddr2("(대항동)");
            requestDto1.setMapX(128.8295937487);
            requestDto1.setMapY(35.0006471157);
            requestDto1.setVisitDate(1);
            requestDto1.setOrderNum(1);

            SaveDestinationRequestDto requestDto2 = new SaveDestinationRequestDto();
            requestDto2.setContentIdx(129156);
            requestDto2.setTitle("가덕도대항인공동굴");
            requestDto2.setAddr1("부산광역시 강서구 외양포로 10");
            requestDto2.setAddr2("");
            requestDto2.setMapX(128.8274685924);
            requestDto2.setMapY(35.0133095493);
            requestDto2.setVisitDate(1);
            requestDto2.setOrderNum(2);

            List<SaveDestinationRequestDto> destinations = new ArrayList<>();
            destinations.add(requestDto1);
            destinations.add(requestDto2);

            String body = objectMapper.writeValueAsString(destinations);

            String accessToken = jwtUtil.createAccess(member.get().getIdx(), "test", "ROLE_USER");
            Cookie accessCookie = new Cookie("access", accessToken);

            //when
            ResultActions result = mockMvc.perform(put("/api/v1/destinations/{idx}", plan.get(0).getIdx())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)
                    .cookie(accessCookie));

            //then
            result
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andDo(document("destinations/update",
                            Preprocessors.preprocessRequest(prettyPrint()),
                            Preprocessors.preprocessResponse(prettyPrint()),
                            requestFields(
                                    fieldWithPath("[].content_idx").description("콘텐츠 인덱스"),
                                    fieldWithPath("[].title").description("관광지 이름"),
                                    fieldWithPath("[].addr1").description("주소1"),
                                    fieldWithPath("[].addr2").description("주소2"),
                                    fieldWithPath("[].mapX").description("위도"),
                                    fieldWithPath("[].mapY").description("경도"),
                                    fieldWithPath("[].visitDate").description("방문 날짜"),
                                    fieldWithPath("[].orderNum").description("순서")
                            ),
                            responseFields(
                                    fieldWithPath("isSuccess").description("성공 여부")
                            )));
        }
    }

    @DisplayName("목적지 추가 테스트")
    @Nested
    class AddDestinationTests {
        @DisplayName("추가 성공")
        @WithMockUser
        @Test
        public void addDestinationSuccess() throws Exception {
            //given
            Optional<Member> member = memberRepository.findByUsername("test");
            Plan plan = planRepository.findByMember(member.get()).get(0);

            AddDestinationRequestDto requestDto = new AddDestinationRequestDto();
            requestDto.setContentIdx(2747097);
            requestDto.setTitle("광교마루길");
            requestDto.setAddr1("수원시 장안구 하광교동 400-10");
            requestDto.setAddr2("");
            requestDto.setMapX(127.0311736602);
            requestDto.setMapY(37.3027052472);
            requestDto.setVisitDate(1);

            String body = objectMapper.writeValueAsString(requestDto);

            String accessToken = jwtUtil.createAccess(member.get().getIdx(), "test", "ROLE_USER");
            Cookie accessCookie = new Cookie("access", accessToken);

            //when
            ResultActions result = mockMvc.perform(post("/api/v1/destinations/{idx}", plan.getIdx())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)
                    .cookie(accessCookie));

            //then
            result
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andDo(document("destinations/add",
                            Preprocessors.preprocessRequest(prettyPrint()),
                            Preprocessors.preprocessResponse(prettyPrint()),
                            requestFields(
                                    fieldWithPath("contentIdx").description("콘텐츠 인덱스"),
                                    fieldWithPath("title").description("관광지 이름"),
                                    fieldWithPath("addr1").description("주소1"),
                                    fieldWithPath("addr2").description("주소2"),
                                    fieldWithPath("mapX").description("위도"),
                                    fieldWithPath("mapY").description("경도"),
                                    fieldWithPath("visitDate").description("방문 날짜")
                            ),
                            responseFields(
                                    fieldWithPath("isSuccess").description("성공 여부")
                            )));
        }
    }

    @DisplayName("목적지 삭제 테스트")
    @Nested
    class DeleteDestinationTests {
        @DisplayName("삭제 성공")
        @WithMockUser
        @Test
        public void deleteDestinationSuccess() throws Exception {
            //given
            Optional<Member> member = memberRepository.findByUsername("test");
            Plan plan = planRepository.findByMember(member.get()).get(0);

            DeleteDestinationRequestDto requestDto = new DeleteDestinationRequestDto();
            requestDto.setOrderNum(1);
            requestDto.setVisitDate(1);

            String body = objectMapper.writeValueAsString(requestDto);

            String accessToken = jwtUtil.createAccess(member.get().getIdx(), "test", "ROLE_USER");
            Cookie accessCookie = new Cookie("access", accessToken);

            //when
            ResultActions result = mockMvc.perform(delete("/api/v1/destinations/{idx}", plan.getIdx())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)
                    .cookie(accessCookie));

            //then
            result
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andDo(document("destinations/delete",
                            Preprocessors.preprocessRequest(prettyPrint()),
                            Preprocessors.preprocessResponse(prettyPrint()),
                            requestFields(
                                    fieldWithPath("visitDate").description("방문 날짜"),
                                    fieldWithPath("orderNum").description("방문 순서")
                            ),
                            responseFields(
                                    fieldWithPath("isSuccess").description("성공 여부")
                            )));
        }
    }
}
