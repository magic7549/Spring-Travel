package com.yong.traeblue.controller.destination;

import com.yong.traeblue.dto.destination.SaveDestinationRequestDto;
import com.yong.traeblue.service.DestinationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class DestinationApiController {
    private final DestinationService destinationService;

    @PutMapping("/destinations/{idx}")
    public ResponseEntity<Map<String, Boolean>> updateDestinations(@PathVariable(name = "idx") Long idx, @RequestBody List<SaveDestinationRequestDto> requestDtoList) {
        boolean isSuccess = destinationService.updateDestinations(idx, requestDtoList);

        return ResponseEntity.ok().body(Collections.singletonMap("isSuccess", isSuccess));
    }
}
