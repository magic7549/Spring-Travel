package com.yong.traeblue.dto.members;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FindPasswordRequestDto {
    private String username;
    private String email;
    private String phone;
}
