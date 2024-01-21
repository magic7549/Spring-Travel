package com.yong.traeblue.service;

import com.yong.traeblue.domain.Member;
import com.yong.traeblue.dto.member.AddMemberRequestDto;
import com.yong.traeblue.repository.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    // 회원가입
    @Transactional
    public boolean save(AddMemberRequestDto request) {
        // 중복 아이디일 경우
        if (memberRepository.existsByUsername(request.getUsername())) {
            return false;
        }

        memberRepository.save(Member.builder()
                .username(request.getUsername())
                .password(bCryptPasswordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .phone(request.getPhone())
                .role("ROLE_USER")
                .build());

        return true;
    }

    // 아이디 중복체크
    public boolean existsUsername(String username) {
        return memberRepository.existsByUsername(username);
    }

    // 아이디 찾기
    public String findUsername(String email, String phone) {
        Optional<Member> member = memberRepository.findByEmailAndPhone(email, phone);
        if (member.isPresent()) {
            return member.get().getUsername();
        }
        else {
            return null;
        }
    }

    // 임시 비밀번호 발급
    public String tempPassword(String username, String email, String phone) {
        Optional<Member> member = memberRepository.findByUsernameAndEmailAndPhone(username, email, phone);

        if (member.isPresent()) {
            // 임시 비밀번호 생성해서 return 하기
            String pwdSet = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
            String tempPwd = "";

            for (int i = 0; i < 8; i++) {
                tempPwd += pwdSet.charAt((int) (Math.random() * pwdSet.length()));
            }

            member.get().setPassword(bCryptPasswordEncoder.encode(tempPwd));
            memberRepository.save(member.get());

            return tempPwd;
        }
        else {
            return null;
        }
    }
}
