package com.yong.traeblue.repository;

import com.yong.traeblue.domain.Destination;
import com.yong.traeblue.domain.Plan;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DestinationRepository extends JpaRepository<Destination, Long> {
    List<Destination> findByPlanAndVisitDate(Plan plan, int visitDate);

    @Transactional
    void deleteByPlanAndVisitDateAndOrderNum(Plan plan, int visitDate, int orderNum);
}
