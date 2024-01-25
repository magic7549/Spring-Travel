package com.yong.traeblue.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yong.traeblue.config.jwt.JWTUtil;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.time.LocalDate;

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
            Long memberIdx = memberRepository.findByUsername("test").get().getIdx();
            planRepository.save(Plan.builder()
                    .memberIdx(memberIdx)
                    .title("First Travel")
                    .startDate(LocalDate.parse("2024-01-12"))
                    .endDate(LocalDate.parse("2024-01-17"))
                    .build());


            String accessToken = jwtUtil.createAccess(memberIdx, "test", "ROLE_USER");
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
                    .andDo(document("plans/find",
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
}
