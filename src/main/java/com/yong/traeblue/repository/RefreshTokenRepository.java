package com.yong.traeblue.repository;

import com.yong.traeblue.domain.RefreshToken;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {

    private final RedisTemplate<String, String> redisTemplate;

    public void save(RefreshToken refreshToken) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        valueOperations.set(refreshToken.getRefreshValue(), refreshToken.getUsername());
        redisTemplate.expire(refreshToken.getRefreshValue(), 14 * 24 * 60 * 60L, TimeUnit.SECONDS);
    }

    public Optional<RefreshToken> findById(String refreshValue) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        String username = valueOperations.get(refreshValue);

        if (Objects.isNull(username)) {
            return Optional.empty();
        }

        return Optional.of(new RefreshToken(refreshValue, username));
    }

    public boolean existsById(String refreshValue) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        return valueOperations.get(refreshValue) != null;
    }

    public void deleteById(String refreshValue) {
        redisTemplate.delete(refreshValue);
    }
}