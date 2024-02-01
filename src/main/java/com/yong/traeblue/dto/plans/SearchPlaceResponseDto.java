package com.yong.traeblue.dto.plans;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SearchPlaceResponseDto {
    private int totalCount;
    private List<PlaceResponseDto> responseDtoList;
}
