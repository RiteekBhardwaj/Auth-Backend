package com.riteek.store.Redis.services;

import com.riteek.store.exceptions.CustomExceptions.ServiceDownException;
import com.riteek.store.exceptions.types.ErrorCodes;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class redisServiceImpl implements redisService {

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public boolean keyExists(String key) {
        try {
            return redisTemplate.hasKey(key);
        } catch (RedisConnectionFailureException ex) {
            throw new ServiceDownException(ErrorCodes.CACHE_DB_SERVICE_UNAVAILABLE, ErrorCodes.CACHE_DB_SERVICE_UNAVAILABLE.getDefaultMessage(), ex);
        }
    }

    @Override
    public boolean setExpiry(String key, Duration ttl) {
        try {
            return redisTemplate.expire(key, ttl);
        } catch (RedisConnectionFailureException ex) {
            throw new ServiceDownException(ErrorCodes.CACHE_DB_SERVICE_UNAVAILABLE, ErrorCodes.CACHE_DB_SERVICE_UNAVAILABLE.getDefaultMessage(), ex);
        }
    }

    @Override
    public boolean setKey(String key, String value, Duration ttl) {
        try {
            return redisTemplate.opsForValue().setIfAbsent(key, value, ttl);
        } catch (RedisConnectionFailureException ex) {
            throw new ServiceDownException(ErrorCodes.CACHE_DB_SERVICE_UNAVAILABLE, ErrorCodes.CACHE_DB_SERVICE_UNAVAILABLE.getDefaultMessage(), ex);
        }
    }

    @Override
    public void setNumberKey(String key) {
        try {
            redisTemplate.opsForValue().increment(key);
        } catch (RedisConnectionFailureException ex) {
            throw new ServiceDownException(ErrorCodes.CACHE_DB_SERVICE_UNAVAILABLE, ErrorCodes.CACHE_DB_SERVICE_UNAVAILABLE.getDefaultMessage(), ex);
        }
    }

    @Override
    public String getValue(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (RedisConnectionFailureException ex) {
            throw new ServiceDownException(ErrorCodes.CACHE_DB_SERVICE_UNAVAILABLE, ErrorCodes.CACHE_DB_SERVICE_UNAVAILABLE.getDefaultMessage(), ex);
        }
    }

    @Override
    public void deleteKeys(String... keys) {
        try {
            redisTemplate.delete(Arrays.asList(keys));
        } catch (RedisConnectionFailureException ex) {
            throw new ServiceDownException(ErrorCodes.CACHE_DB_SERVICE_UNAVAILABLE, ErrorCodes.CACHE_DB_SERVICE_UNAVAILABLE.getDefaultMessage(), ex);
        }
    }

}
