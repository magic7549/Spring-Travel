package com.yong.traeblue.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yong.traeblue.config.jwt.JWTUtil;
import com.yong.traeblue.domain.Destination;
import com.yong.traeblue.domain.Member;
import com.yong.traeblue.domain.Plan;
import com.yong.traeblue.dto.destination.SaveDestinationRequestDto;
import com.yong.traeblue.dto.plans.CreatePlanRequestDto;
import com.yong.traeblue.repository.MemberRepository;
import com.yong.traeblue.repository.PlanRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.*;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
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
class DestinationTest {
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
                .title("First Travel")
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

    @DisplayName("목적지 리스트 업데이트 테스트")
    @Nested
    class UpdateDestinationsTests {
        @DisplayName("업데이트 성공")
        @WithMockUser
        @Test
        public void updateDestinationsSuccess() throws Exception {
            //given
            Optional<Member> member = memberRepository.findByUsername("test");
            Optional<Plan> plan = planRepository.findById(member.get().getIdx());

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
            ResultActions result = mockMvc.perform(put("/api/v1/destinations/{idx}", plan.get().getIdx())
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
}