package com.yong.traeblue.service;

import com.yong.traeblue.domain.Member;
import com.yong.traeblue.dto.member.AddMemberRequestDto;
import com.yong.traeblue.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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

    @DisplayName("멤버 저장 - 성공")
    @Test
    public void saveMember() {
        //given
        MemberService memberService = new MemberService(memberRepository, bCryptPasswordEncoder);
        AddMemberRequestDto request = new AddMemberRequestDto();
        request.setUsername("test");
        request.setPassword("123123123a");
        request.setEmail("test@test.com");
        request.setPhone("01012345678");

        //when
        when(memberRepository.existsByUsername(anyString())).thenReturn(false);

        boolean isSuccess = memberService.save(request);

        //then
        assertThat(isSuccess).isTrue();
    }

    @DisplayName("멤버 저장 - 아이디 중복")
    @Test
    public void saveMemberDuplicate() {
        //given
        MemberService memberService = new MemberService(memberRepository, bCryptPasswordEncoder);
        AddMemberRequestDto request = new AddMemberRequestDto();
        request.setUsername("test");
        request.setPassword("123123123a");
        request.setEmail("test@test.com");
        request.setPhone("01012345678");

        //when
        when(memberRepository.existsByUsername(anyString())).thenReturn(true);

        boolean isSuccess = memberService.save(request);

        //then
        assertThat(isSuccess).isFalse();
    }

    @DisplayName("중복 체크")
    @Test
    public void existsUsername() {
        //given
        MemberService memberService = new MemberService(memberRepository, bCryptPasswordEncoder);
        String username = "tester";
        when(memberRepository.existsByUsername(username)).thenReturn(true);

        //when
        boolean isExists = memberService.existsUsername(username);

        //then
        assertThat(isExists).isTrue();
    }

    @DisplayName("아이디 찾기")
    @Test
    public void findUsername() {
        //given
        MemberService memberService = new MemberService(memberRepository, bCryptPasswordEncoder);
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

    @DisplayName("임시 비밀번호 발급")
    @Test
    public void tempPassword() {
        //given
        MemberService memberService = new MemberService(memberRepository, bCryptPasswordEncoder);
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
}