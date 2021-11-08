package com.technology.lock;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.*;

/**
 * 分布式锁（redis）
 */
@Log4j2
@ConditionalOnProperty(name = "redis.distributed-lock.enabled", havingValue = "true")
@Component
public class RedisDistributedLock {

    private static final long HEARTBEAT_DURATION = 30L;
    private static final ConcurrentHashMap<String, LockMetadata> RUNNING_MAP = new ConcurrentHashMap<>(64);
    private BlockingQueue<Runnable> bkQueue = new ArrayBlockingQueue<>(64);
    private final int POOL_SIZE = 32;

    private final ThreadPoolExecutor THREAD_POOL = new ThreadPoolExecutor(POOL_SIZE, POOL_SIZE, 10L,
            TimeUnit.MINUTES, bkQueue, new ThreadFactoryBuilder().setNameFormat("RedisDistributedLock-heartbeat-pool-%d").build());

    @Autowired
    private RedisService redisService;

    /**
     * 15秒扫描，30秒续期（2倍扫描时间）
     */
    @Scheduled(cron = "10/15 * * * * ?")
    public void heartbeat() {
        for (Map.Entry<String, LockMetadata> entry : RUNNING_MAP.entrySet()) {
            THREAD_POOL.execute(() -> {
                String key = entry.getKey();
                if (!RUNNING_MAP.containsKey(key)) {
                    return;
                }

                LockMetadata metadata = entry.getValue();
                long now = System.currentTimeMillis();
                long expireTime = metadata.getLockTime() + metadata.getTimeout() * 1000;
                long timeout = now > expireTime ? HEARTBEAT_DURATION : (expireTime - now) / 1000;
                if (timeout < HEARTBEAT_DURATION) {
                    timeout = HEARTBEAT_DURATION;
                }
                redisService.set(key, String.valueOf(now), timeout);
                if (log.isDebugEnabled()) {
                    log.debug("[Redis-distributed-lock] update heartbeat, key=[{}], timeout=[{}]. ", key, timeout);
                }
            });
        }
    }

    public boolean tryLock(String redisKey, long keyTimeout) {
        long now = System.currentTimeMillis();
        boolean lock = tryLock(redisKey, keyTimeout, now);
        log.info("[Redis-distributed-lock] Try lock, key=[{}], result=[{}]. ", redisKey, lock);

        if (lock) {
            LockMetadata metadata = new LockMetadata(now, keyTimeout);
            RUNNING_MAP.put(redisKey, metadata);
        }
        return lock;
    }

    public void releaseLock(String key) {
        try {
            redisService.deleteByKey(key);
        } catch (Exception e) {
            log.warn("[Redis-distributed-lock] Release lock failed, key=[{}]. ", key, e);
        }
        RUNNING_MAP.remove(key);
        log.info("[Redis-distributed-lock] Release lock, key=[{}]. ", key);
    }

    private boolean tryLock(String key, long keyTimeout, long now) {
        Boolean firstLock = redisService.setIfAbsent(key, String.valueOf(now), keyTimeout);
        if (Boolean.TRUE.equals(firstLock)) {
            return true;
        }

        Object value = redisService.get(key);
        log.info("[Redis-distributed-lock] get, key=[{}], value=[{}]. ", key, value);
        if (null == value) {
            return false;
        }

        long updateTime = 0;
        try {
            updateTime = Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            log.warn("[Redis-distributed-lock] can not parse lock value, redisKey=[{}], redisValue=[{}]. ", key, value);
        }

        if (updateTime == 0 || isExceedHeartbeat(now, updateTime)) {
            Boolean deleteSuccess = redisService.deleteByKey(key);
            if (!Boolean.TRUE.equals(deleteSuccess)) {
                return false;
            }
            return Boolean.TRUE.equals(redisService.setIfAbsent(key, String.valueOf(now), keyTimeout));
        } else {
            return false;
        }
    }

    private boolean isExceedHeartbeat(long now, long updateTime) {
        long duration = now - updateTime;
        log.info("[Redis-distributed-lock] lock.updateTime=[{}], duration=[{}]. ", updateTime, duration);
        return duration > 3 * HEARTBEAT_DURATION * 1000;
    }

    @AllArgsConstructor
    @Data
    private static class LockMetadata {

        /**
         * 锁定时间戳
         */
        private Long lockTime;

        /**
         * 超时时间，单位：秒
         */
        private Long timeout;

    }

}
