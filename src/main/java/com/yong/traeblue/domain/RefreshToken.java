package com.yong.traeblue.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

//@RedisHash(value = "refreshToken", timeToLive = 60 * 60)
public class RefreshToken {
    @Id
    private String refreshValue;
    private String username;

    public RefreshToken(String refreshValue, String username) {
        this.refreshValue = refreshValue;
        this.username = username;
    }

    public String getRefreshValue() {
        return refreshValue;
    }

    public String getUsername() {
        return username;
    }
}
