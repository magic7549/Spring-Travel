package com.yong.traeblue.repository;

import com.yong.traeblue.domain.Member;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2, replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource("classpath:application.yml") //test용 properties 파일 설정
@DataJpaTest
class MemberRepositoryTest {
    @Autowired
    MemberRepository memberRepository;

    @AfterEach
    public void init() {
        memberRepository.deleteAll();
    }

    @DisplayName("멤버 저장 테스트")
    @Test
    @Transactional
    public void saveMemberTest() {
        //given
        Member member = Member.builder()
                .username("test")
                .password("123123123a")
                .email("test@test.com")
                .phone("01012345678")
                .role("ROLE_USER")
                .build();

        //when
        Member newMember = memberRepository.save(member);

        //then
        assertThat(newMember.getIdx()).isGreaterThan(0);
        assertThat(newMember.getUsername()).isEqualTo("test");
        assertThat(newMember.getPassword()).isEqualTo("123123123a");
        assertThat(newMember.getEmail()).isEqualTo("test@test.com");
        assertThat(newMember.getPhone()).isEqualTo("01012345678");
    }

    @DisplayName("아이디 존재 여부 확인 - 존재함")
    @Test
    @Transactional
    public void findByUsernameExistTest() {
        //given
        Member member = Member.builder()
                .username("test")
                .password("123123123a")
                .email("test@test.com")
                .phone("01012345678")
                .role("ROLE_USER")
                .build();
        memberRepository.save(member);

        //when
        Boolean isExist = memberRepository.existsByUsername("test");

        //then
        assertThat(isExist).isTrue();
    }

    @DisplayName("아이디 존재 여부 확인 - 새로운 아이디")
    @Test
    @Transactional
    public void findByUsernameNewTest() {
        //given
        Member member = Member.builder()
                .username("test")
                .password("123123123a")
                .email("test@test.com")
                .phone("01012345678")
                .role("ROLE_USER")
                .build();
        memberRepository.save(member);

        //when
        Boolean isExist = memberRepository.existsByUsername("test2222");

        //then
        assertThat(isExist).isFalse();
    }

    @DisplayName("이메일 존재 여부 확인 - 존재함")
    @Test
    @Transactional
    public void findByEmailExistTest() {
        //given
        Member member = Member.builder()
                .username("test")
                .password("123123123a")
                .email("test@test.com")
                .phone("01012345678")
                .role("ROLE_USER")
                .build();
        memberRepository.save(member);

        //when
        Boolean isExist = memberRepository.existsByEmail("test@test.com");

        //then
        assertThat(isExist).isTrue();
    }

    @DisplayName("이메일 존재 여부 확인 - 새로운 이메일")
    @Test
    @Transactional
    public void findByEmailNewTest() {
        //given
        Member member = Member.builder()
                .username("test")
                .password("123123123a")
                .email("test@test.com")
                .phone("01012345678")
                .role("ROLE_USER")
                .build();
        memberRepository.save(member);

        //when
        Boolean isExist = memberRepository.existsByEmail("22222@test.com");

        //then
        assertThat(isExist).isFalse();
    }

    @DisplayName("아이디 찾기")
    @Test
    @Transactional
    public void findByEmailAndPhone() {
        //given
        Member member = Member.builder()
                .username("test")
                .password("123123123a")
                .email("test@test.com")
                .phone("01012345678")
                .role("ROLE_USER")
                .build();
        memberRepository.save(member);

        //when
        Optional<Member> findMember = memberRepository.findByEmailAndPhone("test@test.com", "01012345678");

        //then
        assertThat(findMember.get().getUsername()).isEqualTo("test");
        assertThat(findMember.get().getPassword()).isEqualTo("123123123a");
        assertThat(findMember.get().getEmail()).isEqualTo("test@test.com");
        assertThat(findMember.get().getPhone()).isEqualTo("01012345678");
    }

    @DisplayName("아이디, 이메일, 연락처로 member 찾기")
    @Test
    @Transactional
    public void findByUsernameAndEmailAndPhone() {
        //given
        Member member = Member.builder()
                .username("test")
                .password("123123123a")
                .email("test@test.com")
                .phone("01012345678")
                .role("ROLE_USER")
                .build();
        memberRepository.save(member);

        //when
        Optional<Member> findMember = memberRepository.findByUsernameAndEmailAndPhone("test", "test@test.com", "01012345678");

        //then
        assertThat(findMember.get().getUsername()).isEqualTo("test");
        assertThat(findMember.get().getPassword()).isEqualTo("123123123a");
        assertThat(findMember.get().getEmail()).isEqualTo("test@test.com");
        assertThat(findMember.get().getPhone()).isEqualTo("01012345678");
    }
}