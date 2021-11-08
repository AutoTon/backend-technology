package com.technology.aop;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 限流器注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AccessRateLimit {

    /**
     * 指定时间范围内允许的访问次数，默认10次
     * @return 访问次数
     */
    int count() default 10;

    /**
     * 时间区间，默认60
     * @return 时间区间
     */
    long duration() default 60;

    /**
     * 时间单位，默认秒
     * @return 时间单位
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

}
