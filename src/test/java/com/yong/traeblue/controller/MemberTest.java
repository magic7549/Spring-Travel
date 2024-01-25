package com.yong.traeblue.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yong.traeblue.config.jwt.JWTUtil;
import com.yong.traeblue.domain.Member;
import com.yong.traeblue.dto.members.*;
import com.yong.traeblue.repository.MemberRepository;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.util.HashMap;
import java.util.Map;

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
        ResultActions result = mockMvc.perform(get("/api/v1/members/username/{username}", "test"));

        //then
        result
                .andExpect(status().isOk())
                .andExpect(content().json("{\"isAvailable\":false}"))
                .andDo(document("members/signup/username",
                        Preprocessors.preprocessRequest(prettyPrint()),
                        Preprocessors.preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("username").description("아이디")
                        ),
                        responseFields(
                                fieldWithPath("isAvailable").description("아이디 사용 가능 여부").type(JsonFieldType.BOOLEAN)
                        )));
    }

    @DisplayName("회원가입 테스트")
    @Nested
    class SignupTests {
        @DisplayName("회원가입 성공")
        @Test
        public void signupSuccess() throws Exception {
            //given
            AddMemberRequestDto requestDto = new AddMemberRequestDto();
            requestDto.setUsername("test");
            requestDto.setPassword("123123123a");
            requestDto.setEmail("test@test.com");
            requestDto.setPhone("01012345678");

            String body = objectMapper.writeValueAsString(requestDto);

            //when
            ResultActions result = mockMvc.perform(post("/api/v1/members")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            //then
            result
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andDo(document("members/signup",
                            Preprocessors.preprocessRequest(prettyPrint()),
                            Preprocessors.preprocessResponse(prettyPrint()),
                            requestFields(
                                    fieldWithPath("username").description("아이디"),
                                    fieldWithPath("password").description("비밀번호"),
                                    fieldWithPath("email").description("이메일"),
                                    fieldWithPath("phone").description("연락처")
                            ),
                            responseFields(
                                    fieldWithPath("isSuccess").description("회원가입 성공 여부")
                            )));
        }

        @DisplayName("회원가입 실패 - 아이디 중복")
        @Test
        public void signupUsernameFail() throws Exception {
            //given
            memberRepository.save(Member.builder()
                    .username("test")
                    .password(bCryptPasswordEncoder.encode("password123"))
                    .email("test2222@test.com")
                    .phone("01012345678")
                    .build());

            AddMemberRequestDto requestDto = new AddMemberRequestDto();
            requestDto.setUsername("test");
            requestDto.setPassword("123123123a");
            requestDto.setEmail("test@test.com");
            requestDto.setPhone("01012345678");
            String body = objectMapper.writeValueAsString(requestDto);

            //when
            ResultActions result = mockMvc.perform(post("/api/v1/members")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            //then
            result
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("DUPLICATED_USERNAME"));
        }

        @DisplayName("회원가입 실패 - 이메일 중복")
        @Test
        public void signupEmailFail() throws Exception {
            //given
            memberRepository.save(Member.builder()
                    .username("test22222")
                    .password(bCryptPasswordEncoder.encode("password123"))
                    .email("test@test.com")
                    .phone("01012345678")
                    .build());

            AddMemberRequestDto requestDto = new AddMemberRequestDto();
            requestDto.setUsername("test");
            requestDto.setPassword("123123123a");
            requestDto.setEmail("test@test.com");
            requestDto.setPhone("01012345678");
            String body = objectMapper.writeValueAsString(requestDto);

            //when
            ResultActions result = mockMvc.perform(post("/api/v1/members")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            //then
            result
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("DUPLICATED_EMAIL"));
        }
    }

    @DisplayName("아이디 찾기 테스트")
    @Nested
    class FindUsernameTests {
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

            FindUsernameRequestDto requestDto = new FindUsernameRequestDto();
            requestDto.setEmail("test@test.com");
            requestDto.setPhone("01012345678");
            String body = objectMapper.writeValueAsString(requestDto);

            //when
            ResultActions result = mockMvc.perform(post("/api/v1/members/find-username")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            //then
            result
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value("test"))
                    .andDo(document("members/find/username",
                            Preprocessors.preprocessRequest(prettyPrint()),
                            Preprocessors.preprocessResponse(prettyPrint()),
                            requestFields(
                                    fieldWithPath("email").description("이메일"),
                                    fieldWithPath("phone").description("연락처")
                            ),
                            responseFields(
                                    fieldWithPath("username").description("아이디")
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

            FindUsernameRequestDto requestDto = new FindUsernameRequestDto();
            requestDto.setEmail("test222@test.com");
            requestDto.setPhone("01012345678");
            String body = objectMapper.writeValueAsString(requestDto);

            //when
            ResultActions result = mockMvc.perform(post("/api/v1/members/find-username")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            //then
            result
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("NOT_EXISTED_MEMBER"));
        }
    }

    @DisplayName("비밀번호 찾기(임시 비밀번호 발급) 테스트")
    @Nested
    class FindPasswordTests {
        @DisplayName("임시 비밀번호 발급 성공")
        @Test
        public void findPasswordSuccess() throws Exception {
            //given
            memberRepository.save(Member.builder()
                    .username("test")
                    .password("123123123a")
                    .email("test@test.com")
                    .phone("01012345678")
                    .build());

            FindPasswordRequestDto requestDto = new FindPasswordRequestDto();
            requestDto.setUsername("test");
            requestDto.setEmail("test@test.com");
            requestDto.setPhone("01012345678");
            String body = objectMapper.writeValueAsString(requestDto);

            //when
            ResultActions result = mockMvc.perform(post("/api/v1/members/find-password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            //then
            result
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.tempPassword").isNotEmpty())
                    .andExpect(jsonPath("$.tempPassword").isString())
                    .andDo(document("members/find/password",
                            Preprocessors.preprocessRequest(prettyPrint()),
                            Preprocessors.preprocessResponse(prettyPrint()),
                            requestFields(
                                    fieldWithPath("username").description("아이디"),
                                    fieldWithPath("email").description("이메일"),
                                    fieldWithPath("phone").description("연락처")
                            ),
                            responseFields(
                                    fieldWithPath("tempPassword").description("임시 비밀번호")
                            )));
        }

        @DisplayName("임시 비밀번호 발급 실패")
        @Test
        public void findPasswordFail() throws Exception {
            //given
            memberRepository.save(Member.builder()
                    .username("test")
                    .password("123123123a")
                    .email("test@test.com")
                    .phone("01012345678")
                    .build());

            FindPasswordRequestDto requestDto = new FindPasswordRequestDto();
            requestDto.setUsername("test22");
            requestDto.setEmail("test22@test.com");
            requestDto.setPhone("01098765432");
            String body = objectMapper.writeValueAsString(requestDto);

            //when
            ResultActions result = mockMvc.perform(post("/api/v1/members/find-password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            //then
            result
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("NOT_EXISTED_MEMBER"));
        }
    }

    @DisplayName("비밀번호 변경 테스트")
    @Nested
    class ChangePasswordTests {
        @DisplayName("비밀번호 변경 성공")
        @WithMockUser
        @Test
        public void changePasswordSuccess() throws Exception {
            //given
            memberRepository.save(Member.builder()
                    .username("test")
                    .password(bCryptPasswordEncoder.encode("123123123a"))
                    .email("test@test.com")
                    .phone("01012345678")
                    .build());

            ChangePasswordRequestDto requestDto = new ChangePasswordRequestDto();
            requestDto.setCurrentPassword("123123123a");
            requestDto.setNewPassword("9988776655xx");
            String body = objectMapper.writeValueAsString(requestDto);

            String accessToken = jwtUtil.createAccess(1L, "test", "ROLE_USER");
            Cookie accessCookie = new Cookie("access", accessToken);

            //when
            ResultActions result = mockMvc.perform(patch("/api/v1/members/password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)
                    .cookie(accessCookie));

            //then
            result
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andDo(document("members/password",
                            Preprocessors.preprocessRequest(prettyPrint()),
                            Preprocessors.preprocessResponse(prettyPrint()),
                            requestFields(
                                    fieldWithPath("currentPassword").description("현재 비밀번호"),
                                    fieldWithPath("newPassword").description("새로운 비밀번호")
                            ),
                            responseFields(
                                    fieldWithPath("isSuccess").description("비밀번호 변경 성공 여부")
                            )));
        }

        @DisplayName("비밀번호 변경 실패 - 아이디 존재 X")
        @WithMockUser
        @Test
        public void changePasswordFailUsername() throws Exception {
            //given
            memberRepository.save(Member.builder()
                    .username("test")
                    .password(bCryptPasswordEncoder.encode("123123123a"))
                    .email("test@test.com")
                    .phone("01012345678")
                    .build());

            ChangePasswordRequestDto requestDto = new ChangePasswordRequestDto();
            requestDto.setCurrentPassword("123123123a");
            requestDto.setNewPassword("9988776655xx");
            String body = objectMapper.writeValueAsString(requestDto);

            String accessToken = jwtUtil.createAccess(1L, "newuser", "ROLE_USER");
            Cookie accessCookie = new Cookie("access", accessToken);

            //when
            ResultActions result = mockMvc.perform(patch("/api/v1/members/password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)
                    .cookie(accessCookie));

            //then
            result
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("NOT_EXISTED_MEMBER"))
                    .andExpect(jsonPath("$.msg").value("존재하지 않는 회원입니다."));
        }

        @DisplayName("비밀번호 변경 실패 - 비밀번호 일치 X")
        @WithMockUser
        @Test
        public void changePasswordFailPassword() throws Exception {
            //given
            memberRepository.save(Member.builder()
                    .username("test")
                    .password(bCryptPasswordEncoder.encode("123123123a"))
                    .email("test@test.com")
                    .phone("01012345678")
                    .build());

            ChangePasswordRequestDto requestDto = new ChangePasswordRequestDto();
            requestDto.setCurrentPassword("asdfawef2131");
            requestDto.setNewPassword("9988776655xx");
            String body = objectMapper.writeValueAsString(requestDto);

            String accessToken = jwtUtil.createAccess(1L, "test", "ROLE_USER");
            Cookie accessCookie = new Cookie("access", accessToken);

            //when
            ResultActions result = mockMvc.perform(patch("/api/v1/members/password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)
                    .cookie(accessCookie));

            //then
            result
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("WRONG_PASSWORD"))
                    .andExpect(jsonPath("$.msg").value("비밀번호가 일치하지 않습니다."));
        }
    }

    @DisplayName("로그인 테스트")
    @Nested
    class LoginTests {
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

            Map<String, String> requestDto = new HashMap<String, String>();
            requestDto.put("username", "test");
            requestDto.put("password", "password123");
            String body = objectMapper.writeValueAsString(requestDto);

            //when
            ResultActions result = mockMvc.perform(put("/api/v1/members/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            //then
            result
                    .andExpect(status().isOk())
                    .andExpect(cookie().exists("access"))
                    .andExpect(cookie().exists("refresh"))
                    .andDo(document("members/login",
                            Preprocessors.preprocessRequest(prettyPrint()),
                            Preprocessors.preprocessResponse(prettyPrint()),
                            requestFields(
                                    fieldWithPath("username").description("아이디"),
                                    fieldWithPath("password").description("비밀번호")
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

            Map<String, String> requestDto = new HashMap<String, String>();
            requestDto.put("username", "asdf");
            requestDto.put("password", "qwerty1234");
            String body = objectMapper.writeValueAsString(requestDto);

            //when
            ResultActions result = mockMvc.perform(put("/api/v1/members/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));

            //then
            result
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("INVALID_LOGIN"))
                    .andExpect(jsonPath("$.msg").value("아이디 또는 비밀번호가 맞지 않습니다."));
        }
    }

    @DisplayName("로그아웃")
    @WithMockUser
    @Test
    public void logout() throws Exception {
        //when
        ResultActions result = mockMvc.perform(get("/api/v1/members/logout"));

        //then
        result
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @DisplayName("회원 탈퇴 테스트")
    @Nested
    class WithdrawMemberTests {
        @DisplayName("회원 탈퇴 성공")
        @WithMockUser
        @Test
        public void withdrawSuccess() throws Exception {
            //given
            memberRepository.save(Member.builder()
                    .username("test")
                    .password(bCryptPasswordEncoder.encode("123123123a"))
                    .email("test@test.com")
                    .phone("01012345678")
                    .build());

            WithdrawMemberRequestDto requestDto = new WithdrawMemberRequestDto();
            requestDto.setUsername("test");
            requestDto.setPassword("123123123a");
            String body = objectMapper.writeValueAsString(requestDto);

            String refreshToken = jwtUtil.createRefresh(1L, "test", "ROLE_USER");
            Cookie refreshCookie = new Cookie("refresh", refreshToken);

            //when
            ResultActions result = mockMvc.perform(delete("/api/v1/members")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)
                    .cookie(refreshCookie));

            //then
            result
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andDo(document("members/delete",
                            Preprocessors.preprocessRequest(prettyPrint()),
                            Preprocessors.preprocessResponse(prettyPrint()),
                            requestFields(
                                    fieldWithPath("username").description("아이디"),
                                    fieldWithPath("password").description("비밀번호")
                            ),
                            responseFields(
                                    fieldWithPath("isSuccess").description("회원 탈퇴 성공 여부")
                            )));
        }

        @DisplayName("회원 탈퇴 실패")
        @WithMockUser
        @Test
        public void withdrawFail() throws Exception {
            //given
            memberRepository.save(Member.builder()
                    .username("test")
                    .password(bCryptPasswordEncoder.encode("123123123a"))
                    .email("test@test.com")
                    .phone("01012345678")
                    .build());

            WithdrawMemberRequestDto requestDto = new WithdrawMemberRequestDto();
            requestDto.setUsername("test");
            requestDto.setPassword("qwer1234");
            String body = objectMapper.writeValueAsString(requestDto);

            String refreshToken = jwtUtil.createRefresh(1L, "test", "ROLE_USER");
            Cookie refreshCookie = new Cookie("refresh", refreshToken);

            //when
            ResultActions result = mockMvc.perform(delete("/api/v1/members")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)
                    .cookie(refreshCookie));

            //then
            result
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("WRONG_PASSWORD"))
                    .andExpect(jsonPath("$.msg").value("비밀번호가 일치하지 않습니다."));
        }
    }
}
