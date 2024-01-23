package com.yong.traeblue.config.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // 400 error
    UNKNOWN("UNKNOWN", "알 수 없는 에러가 발생했습니다."),
    WRONG_PASSWORD("WRONG_PASSWORD", "비밀번호가 일치하지 않습니다."),
    NOT_EXISTED_MEMBER("NOT_EXISTED_MEMBER", "존재하지 않는 회원입니다."),
    INVALID_LOGIN("INVALID_LOGIN", "아이디 또는 비밀번호가 맞지 않습니다."),
    DUPLICATED_USERNAME("DUPLICATED_USERNAME", "이미 등록되어 있는 아이디입니다."),
    DUPLICATED_EMAIL("DUPLICATED_EMAIL", "이미 등록되어 있는 이메일입니다."),

    // 401 error
    ACCESS_DENIED("ACCESS_DENIED", "유효하지 않은 요청입니다.");

    private final String code;
    private final String msg;
}
