package com.yong.traeblue.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yong.traeblue.config.jwt.JWTUtil;
import com.yong.traeblue.domain.Member;
import com.yong.traeblue.dto.member.AddMemberRequestDto;
import com.yong.traeblue.repository.MemberRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.net.URLEncoder;

import static org.springframework.restdocs.cookies.CookieDocumentation.cookieWithName;
import static org.springframework.restdocs.cookies.CookieDocumentation.responseCookies;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.modifyUris;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureRestDocs
@AutoConfigureMockMvc
@ExtendWith(RestDocumentationExtension.class)
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2, replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource("classpath:application.yml") //test용 properties 파일 설정
public class MemberTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private MemberRepository memberRepository;

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
    }

    @DisplayName("아이디 중복체크")
    @Test
    public void checkUsername() throws Exception {
        //given
        memberRepository.save(Member.builder()
                .username("test")
                .password("123123123a")
                .email("test@test.com")
                .phone("01012345678")
                .build());

        //when
        ResultActions result = mockMvc.perform(get("/api/v1/member/username/{username}", "test"));

        //then
        result
                .andExpect(status().isOk())
                .andExpect(content().json("{\"isAvailable\":false}"))
                .andDo(document("member/signup/username",
                        Preprocessors.preprocessRequest(prettyPrint()),
                        Preprocessors.preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("username").description("아이디")
                        ),
                        responseFields(
                                fieldWithPath("isAvailable").description("아이디 사용 가능 여부").type(JsonFieldType.BOOLEAN)
                        )));
    }

    @DisplayName("아이디 찾기 성공")
    @Test
    public void findUsernameSuccess() throws Exception {
        //given
        memberRepository.save(Member.builder()
                .username("test")
                .password("123123123a")
                .email("test@test.com")
                .phone("01012345678")
                .build());

        //when
        ResultActions result = mockMvc.perform(post("/api/v1/member/find-username")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("email", "test@test.com")
                .param("phone", "01012345678"));

        //then
        result
                .andExpect(status().isOk())
                .andExpect(content().json("{\"username\":test}"))
                .andDo(document("member/find/username",
                        Preprocessors.preprocessRequest(prettyPrint()),
                        Preprocessors.preprocessResponse(prettyPrint()),
                        formParameters(
                                parameterWithName("email").description("이메일"),
                                parameterWithName("phone").description("연락처")
                        ),
                        responseFields(
                                fieldWithPath("username").description("아이디").type(JsonFieldType.STRING)
                        )));
    }

    @DisplayName("아이디 찾기 실패")
    @Test
    public void findUsernameFail() throws Exception {
        //given
        memberRepository.save(Member.builder()
                .username("test")
                .password("123123123a")
                .email("test@test.com")
                .phone("01012345678")
                .build());

        //when
        ResultActions result = mockMvc.perform(post("/api/v1/member/find-username")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("email", "test2222@test.com")
                .param("phone", "01012345678"));

        //then
        result
                .andExpect(status().isNotFound());
    }

    @DisplayName("비밀번호 찾기 성공 - 임시 비밀번호 발급 성공")
    @Test
    public void findPasswordSuccess() throws Exception {
        //given
        memberRepository.save(Member.builder()
                .username("test")
                .password("123123123a")
                .email("test@test.com")
                .phone("01012345678")
                .build());

        //when
        ResultActions result = mockMvc.perform(post("/api/v1/member/find-password")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", "test")
                .param("email", "test@test.com")
                .param("phone", "01012345678"));

        //then
        result
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tempPassword").isNotEmpty())
                .andExpect(jsonPath("$.tempPassword").isString())
                .andDo(document("member/find/password",
                        Preprocessors.preprocessRequest(prettyPrint()),
                        Preprocessors.preprocessResponse(prettyPrint()),
                        formParameters(
                                parameterWithName("username").description("아이디"),
                                parameterWithName("email").description("이메일"),
                                parameterWithName("phone").description("연락처")
                        ),
                        responseFields(
                                fieldWithPath("tempPassword").description("임시 비밀번호").type(JsonFieldType.STRING)
                        )));
    }

    @DisplayName("회원가입 성공")
    @Test
    public void signupSuccess() throws Exception {
        //given
        //when
        ResultActions result = mockMvc.perform(post("/api/v1/member/signup")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", "test")
                .param("password", "123123123a")
                .param("email", "test@test.com")
                .param("phone", "01012345678"));

        //then
        result
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/member/login"))
                .andDo(document("member/signup",
                        Preprocessors.preprocessRequest(prettyPrint()),
                        Preprocessors.preprocessResponse(prettyPrint()),
                        formParameters(
                                parameterWithName("username").description("아이디"),
                                parameterWithName("password").description("비밀번호"),
                                parameterWithName("email").description("이메일"),
                                parameterWithName("phone").description("연락처")
                        )));
    }

    @DisplayName("회원가입 실패 - 아이디 중복")
    @Test
    public void signupFail() throws Exception {
        //given
        memberRepository.save(Member.builder()
                .username("test")
                .password(bCryptPasswordEncoder.encode("password123"))
                .email("test@test.com")
                .phone("01012345678")
                .build());

        //when
        ResultActions result = mockMvc.perform(post("/api/v1/member/signup")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", "test")
                .param("password", "123123123a")
                .param("email", "test@test.com")
                .param("phone", "01012345678"));

        //then
        String errorMessage = URLEncoder.encode("회원가입에 실패하였습니다.", "UTF-8");
        result
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/member/signup?error=true&exception=" + errorMessage));
    }

    @DisplayName("로그인 성공")
    @Test
    public void loginSuccess() throws Exception {
        //given
        memberRepository.save(Member.builder()
                .username("test")
                .password(bCryptPasswordEncoder.encode("password123"))
                .email("test@test.com")
                .phone("01012345678")
                .build());

        //when
        ResultActions result = mockMvc.perform(post("/api/v1/member/login")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", "test")
                .param("password", "password123"));

        //then
        result
                .andExpect(status().is3xxRedirection())
                .andExpect(cookie().exists("access"))
                .andExpect(cookie().exists("refresh"))
                .andDo(document("member/login",
                        Preprocessors.preprocessRequest(prettyPrint()),
                        Preprocessors.preprocessResponse(prettyPrint()),
                        formParameters(
                                parameterWithName("username").description("아이디"),
                                parameterWithName("password").description("비밀번호")
                        ),
                        responseCookies(
                                cookieWithName("access").description("access 토큰"),
                                cookieWithName("refresh").description("refresh 토큰")
                        )));
    }

    @DisplayName("로그인 실패")
    @Test
    public void loginFail() throws Exception {
        //given
        memberRepository.save(Member.builder()
                .username("test")
                .password(bCryptPasswordEncoder.encode("password123"))
                .email("test@test.com")
                .phone("01012345678")
                .build());

        //when
        ResultActions result = mockMvc.perform(post("/api/v1/member/login")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", "test")
                .param("password", "123"));

        //then
        result
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/member/login?error=true&exception=" + URLEncoder.encode("아이디 또는 비밀번호가 맞지 않습니다.", "UTF-8")));
    }

    @DisplayName("로그아웃")
    @WithMockUser
    @Test
    public void logout() throws Exception {
        //when
        ResultActions result = mockMvc.perform(get("/api/v1/member/logout"));

        //then
        result
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @DisplayName("비밀번호 변경")
    @WithMockUser
    @Test
    public void changePassword() throws Exception {
        //given
        memberRepository.save(Member.builder()
                .username("test")
                .password(bCryptPasswordEncoder.encode("123123123a"))
                .email("test@test.com")
                .phone("01012345678")
                .build());

        String accessToken = jwtUtil.createAccess("test", "ROLE_USER");
        Cookie accessCookie = new Cookie("access", accessToken);

        //when
        ResultActions result = mockMvc.perform(put("/api/v1/member/password")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .cookie(accessCookie)
                .param("currentPassword", "123123123a")
                .param("newPassword", "11223344aa"));

        //then
        result
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andDo(document("member/password",
                        Preprocessors.preprocessRequest(prettyPrint()),
                        Preprocessors.preprocessResponse(prettyPrint()),
                        formParameters(
                                parameterWithName("currentPassword").description("현재 비밀번호"),
                                parameterWithName("newPassword").description("새로운 비밀번호")
                        ),
                        responseFields(
                                fieldWithPath("isSuccess").description("비밀번호 변경 성공 여부").type(JsonFieldType.BOOLEAN)
                        )));
    }

    @DisplayName("비밀번호 변경 실패")
    @WithMockUser
    @Test
    public void changePasswordFail() throws Exception {
        //given
        memberRepository.save(Member.builder()
                .username("test")
                .password(bCryptPasswordEncoder.encode("123123123a"))
                .email("test@test.com")
                .phone("01012345678")
                .build());

        String accessToken = jwtUtil.createAccess("test", "ROLE_USER");
        Cookie accessCookie = new Cookie("access", accessToken);

        //when
        ResultActions result = mockMvc.perform(put("/api/v1/member/password")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .cookie(accessCookie)
                .param("currentPassword", "12412fsda")
                .param("newPassword", "11223344aa"));

        //then
        result
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("WRONG_PASSWORD"))
                .andExpect(jsonPath("$.msg").value("현재 비밀번호가 일치하지 않습니다."));
    }
}
