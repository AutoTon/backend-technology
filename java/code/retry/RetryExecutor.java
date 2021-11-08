package com.technology.retry;

import com.google.common.util.concurrent.Uninterruptibles;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class RetryExecutor {

    /**
     * 异常重试执行
     *
     * @param retryTimes      重试次数
     * @param failOnException 失败的时候抛出异常,false的时候不抛出异常，返回<param>defaultValue<param/>
     * @param defaultValue    默认值
     * @param interval        重试间隔
     * @param executor        执行器
     * @return
     */
    public static <T> T execute(int retryTimes, boolean failOnException, T defaultValue, long interval, IExecutor<T> executor) {
        int tryTimes = 0;
        while (tryTimes < retryTimes) {
            try {
                T result = executor.execute();
                if (null == result) {
                    return defaultValue;
                }
                return result;
            } catch (Exception e) {
                tryTimes++;
                log.info("[Executor] retry {} number: [{}]. ", tryTimes, executor);
                if (tryTimes >= retryTimes) {
                    if (failOnException) {
                        log.warn("[Executor] retry fail", e);
                        throw new RuntimeException("RetryExecutor execute failed. ", e);
                    }
                    return defaultValue;
                }
            }
            Uninterruptibles.sleepUninterruptibly(interval, TimeUnit.MILLISECONDS);
        }
        return defaultValue;
    }

}
