package com.project.shopapp.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseDistributedLock {
    private final JdbcTemplate jdbcTemplate;
    private static final Logger logger = LoggerFactory.getLogger(DatabaseDistributedLock.class);

    public boolean acquireLock(String lockKey, int timeoutSeconds) {
        try {
            Boolean result = jdbcTemplate.queryForObject(
                "SELECT GET_LOCK(?, ?)", 
                Boolean.class, 
                lockKey, 
                timeoutSeconds
            );
            if (result) {
                log.debug("Acquired lock: {}", lockKey);
            }
            return result;
        } catch (Exception e) {
            logger.error("Error acquiring lock {}: {}", lockKey, e.getMessage());
            return false;
        }
    }
    
    public void releaseLock(String lockKey) {
        try {
            jdbcTemplate.queryForObject(
                    "SELECT RELEASE_LOCK(?)",
                    Boolean.class,
                    lockKey
            );
        } catch (Exception e) {
            logger.warn("Error releasing lock {}: {}", lockKey, e.getMessage());
        }
    }

}