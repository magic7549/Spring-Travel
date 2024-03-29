package com.yong.traeblue.dto.members;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddMemberRequestDto {
    private String username;
    private String password;
    private String email;
    private String phone;
}
