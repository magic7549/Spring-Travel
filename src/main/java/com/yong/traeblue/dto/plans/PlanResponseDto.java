package com.yong.traeblue.dto.plans;

import com.yong.traeblue.dto.destination.DestinationResponseDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
@Setter
public class PlanResponseDto {
    private String title;
    private String startDate;
    private String endDate;
    private List<DestinationResponseDto> destinations;

    @Builder
    public PlanResponseDto(String title, String startDate, String endDate, List<DestinationResponseDto> destinations) {
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.destinations = destinations;
    }

    public int getTravelDuration() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate start = LocalDate.parse(startDate, formatter);
        LocalDate end = LocalDate.parse(endDate, formatter);

        return (int) start.datesUntil(end.plusDays(1)).count();
    }
}
