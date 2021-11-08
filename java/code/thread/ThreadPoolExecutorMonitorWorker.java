package com.technology.thread;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 监控线程池
 */
@Log4j2
@ConditionalOnProperty(name = "thread-pool.monitor.enabled", havingValue = "true")
@Component
public class ThreadPoolExecutorMonitorWorker {

    private static final String BASE_PACKAGE = "com.xxx";
    private static final String RESOURCE_PATTERN = "/**/*.class";
    private static final int DEFAULT_QUEUE_USAGE_THRESHOLD = 50;
    private static final int DEFAULT_QUEUE_WAITING_THRESHOLD = -1;
    private static final String SPLIT = ":";
    private static final String QUEUE_PERCENTAGE_CONFIG_KEY = "THREAD_POOL_EXECUTOR_ALARM_QUEUE_PERCENTAGE";
    private static final String QUEUE_WAITING_SIZE_CONFIG_KEY = "THREAD_POOL_EXECUTOR_ALARM_QUEUE_WAITING_SIZE";
    private static final String KEEP_ALARM_CONFIG_KEY = "THREAD_POOL_EXECUTOR_KEEP_ALARM";
    private static final String TEMPLATE_DIR = "mail-template/thread-pool-executor-monitor/";

    private static final String IP_ADDRESS = InetAddressUtil.getIpAddress();

    private boolean hasInitial = false;
    private Map<String, ThreadPoolExecutor> executorMap = Maps.newHashMap();
    private Map<String, QueueMonitorConfig> configMap = Maps.newHashMap();
    private Map<String, Date> noticeMap = Maps.newHashMap();
    private Map<String, Integer> queueUsageThresholdMap;
    private Map<String, Integer> queueWaitingThresholdMap;
    private List<String> keepAlarmList;

    @Value("${process.name:}")
    private String processName;

    @Value("${mail.itil-admin}")
    private Set<String> itilAdminMailSet;

    @Autowired
    private Environment env;

    @Autowired
    private SelectConfigService selectConfigService;

    @Autowired
    private NoticeService noticeService;

    /**
     * 注册
     * @param name 线程池名称，全局唯一
     * @param executor 线程池
     * @return 是否注册成功
     */
    public boolean register(String name, ThreadPoolExecutor executor) {
        return register(name, executor, DEFAULT_QUEUE_USAGE_THRESHOLD, DEFAULT_QUEUE_WAITING_THRESHOLD);
    }

    /**
     * 注册
     * @param name 线程池名称，全局唯一
     * @param executor 线程池
     * @param queueUsageThreshold 队列使用率阈值
     * @param queueWaitingThreshold 队列等待任务数量阈值
     * @return 是否注册成功
     */
    public boolean register(String name, ThreadPoolExecutor executor, int queueUsageThreshold, int queueWaitingThreshold) {
        if (executorMap.containsKey(name)) {
            return false;
        }

        QueueMonitorConfig config = QueueMonitorConfig.builder().usageThreshold(queueUsageThreshold).waitingThreshold(queueWaitingThreshold).build();
        configMap.put(name, config);
        executorMap.put(name, executor);
        log.info("[Thread-pool-executor-monitor] Registered executor: [{}]. ", name);
        return true;
    }

    @Scheduled(cron = "55 0/1 * * * ?")
    public void monitor() {
        try {
            if (!hasInitial) {
                init();
                hasInitial = true;
            } else {
                refreshConfigFromDb();
                doMonitor();
            }
        } catch (Throwable t) {
            log.error("[Thread-pool-executor-monitor] execute failure. ", t);
        }
    }

    private void doMonitor() {
        List<ThreadPoolExecutorStatus> alarmList = Lists.newArrayList();
        List<ThreadPoolExecutorStatus> recoverList = Lists.newArrayList();
        for (Map.Entry<String, ThreadPoolExecutor> entry : executorMap.entrySet()) {
            String name = entry.getKey();
            ThreadPoolExecutor executor = entry.getValue();
            ThreadPoolExecutorStatus status = new ThreadPoolExecutorStatus(name, executor);
            if (exceedLimit(name, executor)) {
                if (requireAlarm(name)) {
                    // 刷新通知时间
                    noticeMap.put(name, new Date());
                    alarmList.add(status);
                }
                log.warn("[Thread-pool-executor-monitor] Executor [{}] abnormal status: corePoolSize=[{}], " +
                                "maximumPoolSize=[{}], currentPoolSize=[{}], queueSize=[{}], queueRemainingCapacity=[{}], " +
                                "activeThreadCount=[{}], completedTaskCount=[{}], taskCount=[{}]. ",
                        name, status.getCorePoolSize(), status.getMaximumPoolSize(), status.getCurrentPoolSize(),
                        status.getQueueSize(), status.getQueueRemainingCapacity(),
                        status.getActiveThreadCount(), status.getCompletedTaskCount(), status.getTaskCount());
            } else {
                if (requireRecover(name)) {
                    noticeMap.remove(name);
                    recoverList.add(status);
                }
                if (log.isDebugEnabled()) {
                    log.debug("[Thread-pool-executor-monitor] Executor [{}] status: corePoolSize=[{}], " +
                                    "maximumPoolSize=[{}], currentPoolSize=[{}], queueSize=[{}], queueRemainingCapacity=[{}], " +
                                    "activeThreadCount=[{}], completedTaskCount=[{}], taskCount=[{}]. ",
                            name, status.getCorePoolSize(), status.getMaximumPoolSize(), status.getCurrentPoolSize(),
                            status.getQueueSize(), status.getQueueRemainingCapacity(),
                            status.getActiveThreadCount(), status.getCompletedTaskCount(), status.getTaskCount());
                }
            }
        }

        noticeAlarm(alarmList);
        noticeRecover(recoverList);
    }

    private void init() {
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
                for (Field field : clazz.getDeclaredFields()) {
                    ThreadPoolExecutorMonitor monitor = field.getAnnotation(ThreadPoolExecutorMonitor.class);
                    if (monitor == null) {
                        continue;
                    }

                    Object springBean = null;
                    try {
                        springBean = AopTargetUtils.getTarget(SpringContextUtil.getBean(clazz));
                    } catch (NoSuchBeanDefinitionException e) {
                        if (log.isDebugEnabled()) {
                            log.debug("[Thread-pool-executor-monitor] Not find spring bean: [{}]. ", className);
                        }
                        continue;
                    }

                    field.setAccessible(true);
                    String name = clazz.getName() + "." + field.getName();
                    Object fieldObject = field.get(springBean);
                    if (fieldObject instanceof ThreadPoolExecutor) {
                        register(name, (ThreadPoolExecutor) fieldObject, monitor.queueUsageThreshold(), monitor.queueWaitingThreshold());
                    }
                }
            }
        } catch (ClassNotFoundException | IOException | IllegalAccessException e) {
            log.warn("[Thread-pool-executor-monitor] init failure. ", e);
        }
    }

    private void refreshConfigFromDb() {
        queueUsageThresholdMap = getQueueUsageThresholdMap();
        queueWaitingThresholdMap = getQueueWaitingThresholdMap();
        keepAlarmList = selectConfigService.getSelectValuesByType(KEEP_ALARM_CONFIG_KEY);
    }

    private Map<String, Integer> getConfigMap(String key) {
        Map<String, Integer> map = Maps.newHashMap();
        List<String> list = selectConfigService.getSelectValuesByType(key);
        if (CollectionUtils.isEmpty(list)) {
            return map;
        }

        for (String configValue : list) {
            String[] array = configValue.split(SPLIT);
            if (array.length != 2) {
                continue;
            }

            String name = array[0];
            if (!executorMap.containsKey(name)) {
                continue;
            }

            Integer percentage;
            try {
                percentage = Integer.valueOf(array[1]);
            } catch (NumberFormatException e) {
                continue;
            }

            map.put(name, percentage);
        }

        return map;
    }

    private Map<String, Integer> getQueueUsageThresholdMap() {
        return getConfigMap(QUEUE_PERCENTAGE_CONFIG_KEY);
    }

    private Map<String, Integer> getQueueWaitingThresholdMap() {
        return getConfigMap(QUEUE_WAITING_SIZE_CONFIG_KEY);
    }

    private boolean exceedLimit(String name, ThreadPoolExecutor executor) {
        return checkQueuePercentage(name, executor) || checkQueueWaitingSize(name, executor);
    }

    private boolean checkQueuePercentage(String name, ThreadPoolExecutor executor) {
        int queueSize = executor.getQueue().size();
        int queueRemainingCapacity = executor.getQueue().remainingCapacity();
        if (queueSize == 0 && queueRemainingCapacity == 0) {
            return false;
        }

        int alarmQueuePercentage = DEFAULT_QUEUE_USAGE_THRESHOLD;
        if (queueUsageThresholdMap.containsKey(name)) {
            alarmQueuePercentage = queueUsageThresholdMap.get(name);
        } else if(configMap.containsKey(name)) {
            QueueMonitorConfig config = configMap.get(name);
            if (config.getUsageThreshold() != null) {
                alarmQueuePercentage = config.getUsageThreshold();
            }
        }
        // 负数：不监控
        if (alarmQueuePercentage < 0) {
            return false;
        }

        int currentPercentage = queueSize * 100 / (queueRemainingCapacity + queueSize);
        return currentPercentage >= alarmQueuePercentage;
    }

    private boolean checkQueueWaitingSize(String name, ThreadPoolExecutor executor) {
        int queueSize = executor.getQueue().size();
        int queueRemainingCapacity = executor.getQueue().remainingCapacity();
        if (queueSize == 0 && queueRemainingCapacity == 0) {
            return false;
        }

        int alarmQueueWaitingSize = DEFAULT_QUEUE_WAITING_THRESHOLD;
        if (queueWaitingThresholdMap.containsKey(name)) {
            alarmQueueWaitingSize = queueWaitingThresholdMap.get(name);
        } else if(configMap.containsKey(name)) {
            QueueMonitorConfig config = configMap.get(name);
            if (config.getWaitingThreshold() != null) {
                alarmQueueWaitingSize = config.getWaitingThreshold();
            }
        }
        // 负数：不监控
        if (alarmQueueWaitingSize < 0) {
            return false;
        }

        return queueSize >= alarmQueueWaitingSize;
    }

    private boolean requireAlarm(String name) {
        Date now = new Date();
        Date lastNoticeTime = noticeMap.get(name);
        if (null == lastNoticeTime) {
            return true;
        } else {
            if (keepAlarmList.contains(name)) {
                return true;
            }

            long days = DateUtil.diffDay(now, lastNoticeTime);
            return days >= 1;
        }
    }

    private boolean requireRecover(String name) {
        return noticeMap.containsKey(name);
    }

    private void noticeAlarm(List<ThreadPoolExecutorStatus> statusList) {
        if (CollectionUtils.isEmpty(statusList)) {
            return;
        }

        Map<String, Object> params = new HashMap<>(4);
        params.put("statusList", statusList);
        params.put("processName", processName);
        params.put("env", env.getActiveProfiles()[0]);
        params.put("ip", IP_ADDRESS);

        Set<String> receivers = Sets.newHashSet(itilAdminMailSet);
        noticeService.sendTemplateMsg(TEMPLATE_DIR, "alarm", params, receivers, null);
        log.info("[Thread-pool-executor-monitor] send alarm notice, receivers=[{}]. ", receivers);
    }

    private void noticeRecover(List<ThreadPoolExecutorStatus> statusList) {
        if (CollectionUtils.isEmpty(statusList)) {
            return;
        }

        Map<String, Object> params = new HashMap<>(4);
        params.put("statusList", statusList);
        params.put("processName", processName);
        params.put("env", env.getActiveProfiles()[0]);
        params.put("ip", IP_ADDRESS);

        Set<String> receivers = Sets.newHashSet(itilAdminMailSet);
        noticeService.sendTemplateMsg(TEMPLATE_DIR, "recover", params, receivers, null);
        log.info("[Thread-pool-executor-monitor] send recover notice, receivers=[{}]. ", receivers);
    }

}
