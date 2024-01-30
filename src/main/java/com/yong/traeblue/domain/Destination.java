package com.yong.traeblue.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Table(name = "destination")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Entity
public class Destination {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idx", nullable = false, updatable = false)
    private Long idx;

    @ManyToOne
    @JoinColumn(name = "plan_idx", referencedColumnName = "idx", nullable = false)
    private Plan plan;

    @Column(name = "content_idx")
    private int contentIdx;

    @Column(name = "title", length = 30)
    private String title;

    @Column(name = "addr1", length = 100)
    private String addr1;

    @Column(name = "addr2", length = 50)
    private String addr2;

    @Column(name = "mapX")
    private double mapX;

    @Column(name = "mapY")
    private double mapY;

    @Column(name = "visit_date")
    private int visitDate;

    @Column(name = "order_num")
    private int orderNum;

    @Builder
    public Destination(Plan plan, int contentIdx, String title, String addr1, String addr2, double mapX, double mapY, int visitDate, int orderNum) {
        this.plan = plan;
        this.contentIdx = contentIdx;
        this.title = title;
        this.addr1 = addr1;
        this.addr2 = addr2;
        this.mapX = mapX;
        this.mapY = mapY;
        this.visitDate = visitDate;
        this.orderNum = orderNum;
    }
}
