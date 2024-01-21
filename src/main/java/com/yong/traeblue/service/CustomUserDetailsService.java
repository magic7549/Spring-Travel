package com.yong.traeblue.service;

import com.yong.traeblue.domain.Member;
import com.yong.traeblue.dto.CustomUserDetails;
import com.yong.traeblue.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member member = memberRepository.findByUsername(username);

        if (member != null) {
            return new CustomUserDetails(member);
        }

        throw new UsernameNotFoundException("존재하지 않는 아이디입니다.");
    }
}
