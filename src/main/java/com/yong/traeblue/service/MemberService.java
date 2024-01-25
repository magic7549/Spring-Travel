package com.yong.traeblue.service;

import com.yong.traeblue.config.exception.CustomException;
import com.yong.traeblue.config.exception.ErrorCode;
import com.yong.traeblue.domain.Member;
import com.yong.traeblue.repository.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    // 아이디 중복체크
    public boolean existsUsername(String username) {
        return memberRepository.existsByUsername(username);
    }

    // 회원가입
    @Transactional
    public boolean save(String username, String password, String email, String phone) {
        // 중복 아이디일 경우
        if (memberRepository.existsByUsername(username)) {
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.DUPLICATED_USERNAME);
        }

        // 중복 이메일일 경우
        if (memberRepository.existsByEmail(email)) {
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.DUPLICATED_EMAIL);
        }

        memberRepository.save(Member.builder()
                .username(username)
                .password(bCryptPasswordEncoder.encode(password))
                .email(email)
                .phone(phone)
                .role("ROLE_USER")
                .build());

        return true;
    }

    // 아이디 찾기
    public String findUsername(String email, String phone) {
        Member member = memberRepository.findByEmailAndPhone(email, phone).orElseThrow(() -> new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_EXISTED_MEMBER));
        return member.getUsername();
    }

    // 임시 비밀번호 발급
    public String tempPassword(String username, String email, String phone) {
        Member member = memberRepository.findByUsernameAndEmailAndPhone(username, email, phone).orElseThrow(() -> new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_EXISTED_MEMBER));

        // 임시 비밀번호 생성해서 return 하기
        String pwdSet = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String tempPwd = "";
        for (int i = 0; i < 8; i++) {
            tempPwd += pwdSet.charAt((int) (Math.random() * pwdSet.length()));
        }

        member.setPassword(bCryptPasswordEncoder.encode(tempPwd));
        memberRepository.save(member);

        return tempPwd;
    }

    // 비밀번호 변경
    public boolean changePassword(String currentPassword, String newPassword, Long idx) {
        Member member = memberRepository.findById(idx).orElseThrow(() -> new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_EXISTED_MEMBER));

        if (bCryptPasswordEncoder.matches(currentPassword, member.getPassword())) {
            member.setPassword(bCryptPasswordEncoder.encode(newPassword));
            memberRepository.save(member);
            return true;
        } else {
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.WRONG_PASSWORD);
        }
    }

    // 회원 탈퇴
    public boolean withdrawMember(String username, String password) {
        Member member = memberRepository.findByUsername(username).orElseThrow(() -> new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_EXISTED_MEMBER));

        if (bCryptPasswordEncoder.matches(password, member.getPassword())) {
            memberRepository.delete(member);
            return true;
        } else {
            throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.WRONG_PASSWORD);
        }
    }
}
