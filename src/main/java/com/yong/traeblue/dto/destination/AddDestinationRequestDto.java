package com.yong.traeblue.dto.destination;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddDestinationRequestDto {
    private Long planIdx;
    private int contentIdx;
    private String title;
    private String addr1;
    private String addr2;
    private double mapX;
    private double mapY;
    private int visitDate;
    private int orderNum;
}
