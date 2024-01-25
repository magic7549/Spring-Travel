package com.yong.traeblue.dto.plans;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MyPlanListResponseDto {
    private Long idx;
    private String title;
    private String startDate;
    private String endDate;

    @Builder
    public MyPlanListResponseDto(Long idx, String title, String startDate, String endDate) {
        this.idx = idx;
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
