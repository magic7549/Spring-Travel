package com.yong.traeblue.service;

import com.yong.traeblue.config.exception.CustomException;
import com.yong.traeblue.config.exception.ErrorCode;
import com.yong.traeblue.domain.Member;
import com.yong.traeblue.dto.members.AddMemberRequestDto;
import com.yong.traeblue.repository.MemberRepository;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(RestDocumentationExtension.class)
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2, replace = AutoConfigureTestDatabase.Replace.ANY)
@ExtendWith(MockitoExtension.class)
class MemberServiceTest {
    @Mock
    MemberRepository memberRepository;

    @Mock
    BCryptPasswordEncoder bCryptPasswordEncoder;

    @InjectMocks
    MemberService memberService;

    @DisplayName("중복 체크")
    @Test
    public void existsUsername() {
        //given
        String username = "tester";
        when(memberRepository.existsByUsername(username)).thenReturn(true);

        //when
        boolean isExists = memberService.existsUsername(username);

        //then
        assertThat(isExists).isTrue();
    }

    @DisplayName("멤버 저장 테스트")
    @Nested
    class SaveMemberTests {
        @DisplayName("멤버 저장 - 성공")
        @Test
        public void saveMemberSuccess() {
            //given
            AddMemberRequestDto request = new AddMemberRequestDto();
            request.setUsername("test");
            request.setPassword("123123123a");
            request.setEmail("test@test.com");
            request.setPhone("01012345678");

            when(memberRepository.existsByUsername(anyString())).thenReturn(false);

            //when
            boolean isSuccess = memberService.save(request.getUsername(), request.getPassword(), request.getEmail(), request.getPhone());

            //then
            assertThat(isSuccess).isTrue();
        }

        @DisplayName("멤버 저장 - 아이디 중복")
        @Test
        public void saveMemberUsernameDuplicate() {
            //given
            AddMemberRequestDto request = new AddMemberRequestDto();
            request.setUsername("test");
            request.setPassword("123123123a");
            request.setEmail("test@test.com");
            request.setPhone("01012345678");

            //when
            when(memberRepository.existsByUsername(anyString())).thenReturn(true);

            try {
                boolean isSuccess = memberService.save(request.getUsername(), request.getPassword(), request.getEmail(), request.getPhone());
            } catch (CustomException e) {
                //then
                assertThat(e.getErrorCode()).isEqualTo(ErrorCode.DUPLICATED_USERNAME);
            }
        }

        @DisplayName("멤버 저장 - 이메일 중복")
        @Test
        public void saveMemberEmailDuplicate() {
            //given
            AddMemberRequestDto request = new AddMemberRequestDto();
            request.setUsername("test");
            request.setPassword("123123123a");
            request.setEmail("test@test.com");
            request.setPhone("01012345678");

            //when
            when(memberRepository.existsByEmail(anyString())).thenReturn(true);

            try {
                boolean isSuccess = memberService.save(request.getUsername(), request.getPassword(), request.getEmail(), request.getPhone());
            } catch (CustomException e) {
                //then
                assertThat(e.getErrorCode()).isEqualTo(ErrorCode.DUPLICATED_EMAIL);
            }
        }
    }

    @DisplayName("멤버 저장 테스트")
    @Nested
    class FindUsernameTests {
        @DisplayName("아이디 찾기 - 성공")
        @Test
        public void findUsernameSuccess() {
            //given
            String email = "test@test.com";
            String phone = "01012345678";
            when(memberRepository.findByEmailAndPhone(email, phone)).thenReturn(Optional.ofNullable(Member.builder()
                    .username("test")
                    .password("123123123a")
                    .email("test@test.com")
                    .phone("01012345678")
                    .build()));

            //when
            String username = memberService.findUsername(email, phone);

            //then
            assertThat(username).isEqualTo("test");
        }

        @DisplayName("아이디 찾기 - 실패")
        @Test
        public void findUsernameFail() {
            //given
            String email = "test@test.com";
            String phone = "01012345678";
            when(memberRepository.findByEmailAndPhone(email, phone)).thenThrow(new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_EXISTED_MEMBER));

            //when
            try {
                String username = memberService.findUsername(email, phone);
            } catch (CustomException e) {
                //then
                assertThat(e.getErrorCode()).isEqualTo(ErrorCode.NOT_EXISTED_MEMBER);
            }
        }
    }

    @DisplayName("임시 비밀번호 발급 테스트")
    @Nested
    class TempPasswordTests {
        @DisplayName("임시 비밀번호 발급 - 성공")
        @Test
        public void tempPasswordSuccess() {
            //given
            String username = "test";
            String email = "test@test.com";
            String phone = "01012345678";
            when(memberRepository.findByUsernameAndEmailAndPhone(username, email, phone)).thenReturn(Optional.ofNullable(Member.builder()
                    .username("test")
                    .password("123123123a")
                    .email("test@test.com")
                    .phone("01012345678")
                    .build()));

            //when
            String tempPassword = memberService.tempPassword(username, email, phone);

            //then
            assertThat(tempPassword).isNotEqualTo("123123123a");
        }

        @DisplayName("임시 비밀번호 발급 - 실패")
        @Test
        public void tempPasswordFail() {
            //given
            String username = "test";
            String email = "test@test.com";
            String phone = "01012345678";
            when(memberRepository.findByUsernameAndEmailAndPhone(username, email, phone)).thenThrow(new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_EXISTED_MEMBER));

            //when
            try {
                String tempPassword = memberService.tempPassword(username, email, phone);
            } catch (CustomException e) {
                //then
                assertThat(e.getErrorCode()).isEqualTo(ErrorCode.NOT_EXISTED_MEMBER);
            }
        }
    }

    @DisplayName("비밀번호 변경 테스트")
    @Nested
    class ChangePassword {
        @DisplayName("비밀번호 변경 성공")
        @Test
        public void changePasswordSuccess() {
            //given
            Long idx = 1L;
            String username = "test";
            String email = "test@test.com";
            String phone = "01012345678";

            when(memberRepository.findById(idx)).thenReturn(Optional.ofNullable(Member.builder()
                    .username("test")
                    .password("$2a$10$eVIuyJVT/JfHCd85Stosh.m/sJna.czphUnf.0VEdTuiK.DjENrnq")
                    .email("test@test.com")
                    .phone("01012345678")
                    .build()));
            when(bCryptPasswordEncoder.matches(anyString(), anyString())).thenReturn(true);

            //when
            boolean isChangeSuccess = memberService.changePassword("123123123a", "11223344aa", idx);

            //then
            assertThat(isChangeSuccess).isTrue();
        }

        @DisplayName("비밀번호 변경 실패 - 아이디 존재 X")
        @Test
        public void changePasswordFailUsername() {
            //given
            Long idx = 1L;
            String username = "test";
            String email = "test@test.com";
            String phone = "01012345678";

            when(memberRepository.findById(idx)).thenThrow(new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_EXISTED_MEMBER));

            //when
            try {
                boolean isChangeSuccess = memberService.changePassword("123123123a", "11223344aa", idx);
            } catch (CustomException e) {
                //then
                assertThat(e.getErrorCode()).isEqualTo(ErrorCode.NOT_EXISTED_MEMBER);
            }
        }

        @DisplayName("비밀번호 변경 실패 - 비밀번호 일치 X")
        @Test
        public void changePasswordFailPassword() {
            //given
            Long idx = 1L;
            String username = "test";
            String email = "test@test.com";
            String phone = "01012345678";

            when(memberRepository.findById(idx)).thenReturn(Optional.ofNullable(Member.builder()
                    .username("test")
                    .password("$2a$10$eVIuyJVT/JfHCd85Stosh.m/sJna.czphUnf.0VEdTuiK.DjENrnq")
                    .email("test@test.com")
                    .phone("01012345678")
                    .build()));
            when(bCryptPasswordEncoder.matches(anyString(), anyString())).thenReturn(false);


            //when
            try {
                boolean isChangeSuccess = memberService.changePassword("123123123a", "11223344aa", idx);
            } catch (CustomException e) {
                //then
                assertThat(e.getErrorCode()).isEqualTo(ErrorCode.WRONG_PASSWORD);
            }
        }
    }

    @DisplayName("회원 탈퇴 테스트")
    @Nested
    class WithdrawMemberTests {
        @DisplayName("비밀번호 변경 성공")
        @Test
        public void withdrawMemberSuccess() {
            //given
            when(memberRepository.findByUsername("test")).thenReturn(Optional.ofNullable(Member.builder()
                    .username("test")
                    .password("$2a$10$eVIuyJVT/JfHCd85Stosh.m/sJna.czphUnf.0VEdTuiK.DjENrnq")
                    .email("test@test.com")
                    .phone("01012345678")
                    .build()));
            when(bCryptPasswordEncoder.matches(anyString(), anyString())).thenReturn(true);

            //when
            boolean isSuccess = memberService.withdrawMember("test", "qwer1234");

            //then
            assertThat(isSuccess).isTrue();
        }

        @DisplayName("비밀번호 변경 실패 - 아이디 존재 X")
        @Test
        public void withdrawMemberFailUsername() {
            //given
            when(memberRepository.findByUsername("test")).thenThrow(new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_EXISTED_MEMBER));

            //when
            try {
                boolean isSuccess = memberService.withdrawMember("test", "qwer1234");
            } catch (CustomException e) {
                //then
                assertThat(e.getErrorCode()).isEqualTo(ErrorCode.NOT_EXISTED_MEMBER);
            }
        }

        @DisplayName("비밀번호 변경 실패 - 비밀번호 일치 X")
        @Test
        public void withdrawMemberFailPassword() {
            //given
            when(memberRepository.findByUsername("test")).thenReturn(Optional.ofNullable(Member.builder()
                    .username("test")
                    .password("$2a$10$eVIuyJVT/JfHCd85Stosh.m/sJna.czphUnf.0VEdTuiK.DjENrnq")
                    .email("test@test.com")
                    .phone("01012345678")
                    .build()));
            when(bCryptPasswordEncoder.matches(anyString(), anyString())).thenReturn(false);

            //when
            try {
                boolean isSuccess = memberService.withdrawMember("test", "qwer1234");
            } catch (CustomException e) {
                //then
                assertThat(e.getErrorCode()).isEqualTo(ErrorCode.WRONG_PASSWORD);
            }
        }
    }
}