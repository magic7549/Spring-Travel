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
    ACCESS_DENIED("ACCESS_DENIED", "유효하지 않은 요청입니다.");

    private final String code;
    private final String msg;
}
