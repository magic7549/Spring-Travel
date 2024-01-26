package com.yong.traeblue.repository;

import com.yong.traeblue.domain.Member;
import com.yong.traeblue.domain.Plan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlanRepository extends JpaRepository<Plan, Long> {
    List<Plan> findByMember(Member member);
}
