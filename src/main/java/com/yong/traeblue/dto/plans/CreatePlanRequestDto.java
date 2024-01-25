package com.yong.traeblue.dto.plans;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatePlanRequestDto {
    private String title;
    private String startDate;
    private String endDate;
}
