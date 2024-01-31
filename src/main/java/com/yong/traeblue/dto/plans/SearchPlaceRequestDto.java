package com.yong.traeblue.dto.plans;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchPlaceRequestDto {
    private String pageNo;
    private String keyword;
    private String areaCode;
    private String sigunguCode;
}
