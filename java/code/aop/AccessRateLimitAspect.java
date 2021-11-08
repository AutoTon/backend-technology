package com.technology.aop;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 限流器
 */
@Log4j2
@Component
@Aspect
public class AccessRateLimitAspect {

    private static final String BASE_PACKAGE = "com.xxx";
    private static final String RESOURCE_PATTERN = "/**/*.class";
    private static final String WHITELIST_CONFIG_KEY = "ACCESS_RATE_LIMIT_WHITELIST";
    private static final ConcurrentHashMap<String, Cache<Long, Integer>> MAP = new ConcurrentHashMap<>(16);

    @Autowired
    private SelectConfigService selectConfigService;

    @Pointcut("@annotation(com.technology.aop.AccessRateLimit)")
    public void accessRateLimit() {

    }

    @Before("accessRateLimit()")
    public void pointcut(JoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        String methodName = method.getDeclaringClass().getName() + "." + method.getName();
        if (log.isDebugEnabled()) {
            log.debug("[Access-rate-limit-aspect] method: {}. ", methodName);
        }
        AccessRateLimit accessRateLimit = AnnotationUtils.findAnnotation(method, AccessRateLimit.class);
        if (null == accessRateLimit) {
            return;
        }

        // 白名单则不受限制
        if (isInWhitelist()) {
            return;
        }

        int count = accessRateLimit.count();
        Cache<Long, Integer> cache = MAP.get(methodName);
        boolean isAllowed = false;
        synchronized (methodName.intern()) {
            // 清除过期缓存，再去获取数量
            cache.cleanUp();
            if (cache.size() < count) {
                cache.put(System.currentTimeMillis(), 0);
                log.info("[Access-rate-limit-aspect] allow access method: [{}], count: [{}]. ", methodName, cache.size());
                isAllowed = true;
            }
        }

        if (!isAllowed) {
            throw new CmnbDetailException("请求频率已超限，请稍后重试");
        }
    }

    @PostConstruct
    public void init() {
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        try {
            String pattern = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
                    ClassUtils.convertClassNameToResourcePath(BASE_PACKAGE) + RESOURCE_PATTERN;
            Resource[] resources = resourcePatternResolver.getResources(pattern);
            MetadataReaderFactory readerFactory = new CachingMetadataReaderFactory(resourcePatternResolver);
            for (Resource resource : resources) {
                MetadataReader reader = readerFactory.getMetadataReader(resource);
                String className = reader.getClassMetadata().getClassName();
                Class<?> clazz = Class.forName(className);
                for (Method method : clazz.getMethods()) {
                    AccessRateLimit accessRateLimit = method.getAnnotation(AccessRateLimit.class);
                    if (accessRateLimit != null) {
                        String key = clazz.getName() + "." + method.getName();
                        log.info("[Access-rate-limit-aspect] find limit method: [{}], count=[{}], duration=[{}], timeUnit=[{}]. ",
                                key, accessRateLimit.count(), accessRateLimit.duration(), accessRateLimit.timeUnit());
                        Cache<Long, Integer> cache = CacheBuilder.newBuilder()
                                .initialCapacity(Math.min(10, accessRateLimit.count()))
                                .maximumSize(accessRateLimit.count())
                                .expireAfterWrite(accessRateLimit.duration(), accessRateLimit.timeUnit())
                                .build();
                        MAP.put(key, cache);
                    }
                }
            }
        } catch (ClassNotFoundException | IOException e) {
            log.warn("[Access-rate-limit-aspect] init failure. ", e);
        }
    }

    private boolean isInWhitelist() {
        String operator = UsernameHolder.getUsername();
        List<String> passList = selectConfigService.getSelectValuesByType(WHITELIST_CONFIG_KEY);
        return passList.contains(operator);
    }

}
