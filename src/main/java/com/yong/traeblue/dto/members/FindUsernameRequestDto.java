package com.yong.traeblue.dto.members;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FindUsernameRequestDto {
    private String email;
    private String phone;
}
