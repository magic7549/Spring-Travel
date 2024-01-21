package com.yong.traeblue.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;

@Table(name = "plan")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class Plan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idx", nullable = false, updatable = false)
    private Long idx;

    @Column(name = "member_idx")
    private Long memberIdx;

    @Column(name = "title", length = 30)
    private String title;

    @Column(name = "start_date")
    private Date startDate;

    @Column(name = "end_date")
    private Date endDate;

    @Builder
    public Plan(Long memberIdx, String title, Date startDate, Date endDate) {
        this.memberIdx = memberIdx;
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
