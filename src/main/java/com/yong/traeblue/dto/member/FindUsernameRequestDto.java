package com.yong.traeblue.dto.member;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FindUsernameRequestDto {
    private String email;
    private String phone;
}
