package com.yong.traeblue.dto.destination;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SaveDestinationRequestDto {
    @JsonProperty("content_idx")
    private int contentIdx;
    private String title;
    private String addr1;
    private String addr2;
    private double mapX;
    private double mapY;
    private int visitDate;
    private int orderNum;
}
