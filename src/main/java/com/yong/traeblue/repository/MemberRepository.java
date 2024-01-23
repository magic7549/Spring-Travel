package com.yong.traeblue.repository;

import com.yong.traeblue.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);

    Optional<Member> findByUsername(String username);
    Optional<Member> findByEmailAndPhone(String email, String phone);
    Optional<Member> findByUsernameAndEmailAndPhone(String username, String email, String phone);
}
