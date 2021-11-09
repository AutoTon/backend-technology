package com.technology.apollo;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfig;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfigChangeListener;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggerConfiguration;
import org.springframework.boot.logging.LoggingSystem;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * 动态修改日志级别
 * 使用方式：
 * （1）注册到spring容器：
 * @Bean
 * public LoggerRefreshConfig loggerRefreshConfig() {
 *   return new LoggerRefreshConfig();
 * }
 * （2）在apollo修改指定配置：
 * # logging.level.{your logger}={日志级别}
 * ## root
 * logging.level.root = info
 *
 * ## 包级别
 * logging.level.org.springframework.web = info
 *
 * ## 类级别
 * logging.level.org.springframework.web.servlet.HandlerInterceptor = info
 */
@Slf4j
public class LoggerRefreshConfig {

    private static final String PREFIX = "logging.level.";
    private static final String ROOT = LoggingSystem.ROOT_LOGGER_NAME;
    private static final String SPLIT = ".";

    @Resource
    private LoggingSystem loggingSystem;

    @ApolloConfig
    private Config config;

    @PostConstruct
    private void init() {
        refreshLoggingLevels(config.getPropertyNames());
    }

    @ApolloConfigChangeListener(interestedKeyPrefixes = PREFIX)
    private void onChange(ConfigChangeEvent changeEvent) {
        refreshLoggingLevels(changeEvent.changedKeys());
    }

    private void refreshLoggingLevels(Set<String> changedKeys) {
        for (String key : changedKeys) {
            // key may be : logging.level.com.example.web
            if (StringUtils.startsWithIgnoreCase(key, PREFIX)) {
                String loggerName = PREFIX.equalsIgnoreCase(key) ? ROOT : key.substring(PREFIX.length());
                String strLevel = config.getProperty(key, parentStrLevel(loggerName));
                LogLevel level = LogLevel.valueOf(strLevel.toUpperCase());
                loggingSystem.setLogLevel(loggerName, level);
                log(loggerName, strLevel);
            }
        }
    }

    private String parentStrLevel(String loggerName) {
        String parentLoggerName = loggerName.contains(SPLIT) ? loggerName.substring(0, loggerName.lastIndexOf(SPLIT)) : ROOT;
        return loggingSystem.getLoggerConfiguration(parentLoggerName).getEffectiveLevel().name();
    }

    private void log(String loggerName, String strLevel) {
        try {
            LoggerConfiguration loggerConfiguration = loggingSystem.getLoggerConfiguration(log.getName());
            Method method = log.getClass().getMethod(loggerConfiguration.getEffectiveLevel().name().toLowerCase(), String.class, Object.class, Object.class);
            method.invoke(log, "changed {} log level to:{}", loggerName, strLevel);
        } catch (Exception e) {
            log.error("changed {} log level to:{} FAILED", loggerName, strLevel, e);
        }
    }
}