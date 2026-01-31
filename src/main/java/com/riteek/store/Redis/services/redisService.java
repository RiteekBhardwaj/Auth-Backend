package com.riteek.store.Redis.services;

import java.time.Duration;

public interface redisService {
    String getValue(String email);
    void deleteKeys(String... Keys);
    boolean keyExists(String key);
    boolean setExpiry(String key, Duration ttl);
    boolean setKey(String key, String value, Duration ttl);
    void setNumberKey(String key);
}
