package com.yong.traeblue.dto.member;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordRequestDto {
    private String currentPassword;
    private String newPassword;
}
