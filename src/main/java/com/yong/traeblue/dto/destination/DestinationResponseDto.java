package com.yong.traeblue.dto.destination;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DestinationResponseDto {
    private int content_idx;
    private String title;
    private String addr1;
    private String addr2;
    private double mapX;
    private double mapY;
    private int visitDate;
    private int orderNum;

    @Builder
    public DestinationResponseDto(int content_idx, String title, String addr1, String addr2, double mapX, double mapY, int visitDate, int orderNum) {
        this.content_idx = content_idx;
        this.title = title;
        this.addr1 = addr1;
        this.addr2 = addr2;
        this.mapX = mapX;
        this.mapY = mapY;
        this.visitDate = visitDate;
        this.orderNum = orderNum;
    }
}
