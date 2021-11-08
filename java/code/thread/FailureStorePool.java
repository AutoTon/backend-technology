package com.technology.thread;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;

/**
 * 存储失败任务的线程池-自动重试
 */
@Log4j2
@Component
public class FailureStorePool {

    /**
     * 邮件网关的并发请求上限为10
     */
    private static final int CORE_POOL_SIZE = 6;
    private static final int MAXIMUM_POOL_SIZE = 10;
    private static final long KEEP_ALIVE_MINUTE = 5L;

    private Executor executor;

    public FailureStorePool() {
        BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>(1000);
        ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setNameFormat("FailureStorePool-thread-%d")
                .build();
        this.executor = new Executor(
                CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE_MINUTE, TimeUnit.MINUTES, queue, threadFactory
        );
    }

    /**
     * 执行任务
     * @param runnable 任务
     */
    public void execute(Runnable runnable) {
        this.executor.execute(runnable);
    }

    /**
     * 对失败的任务重新放到线程池执行
     */
    public void retry() {
        List<Runnable> failureList = this.executor.listFailure();
        for (Runnable runnable : failureList) {
            this.executor.execute(runnable);
        }
    }

    /**
     * 清理失败的任务
     */
    public void clearFailure() {
        this.executor.clearFailure();
    }

    public class Executor extends ThreadPoolExecutor {

        @Getter
        private Queue<Runnable> failureQueue = new LinkedBlockingQueue<>(500);
        private final Object LOCK = new Object();

        Executor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
        }

        @Override
        protected void afterExecute(Runnable r, Throwable t) {
            super.afterExecute(r, t);
            if (t != null) {
                boolean isSuccess = false;
                synchronized (LOCK) {
                    isSuccess = failureQueue.add(r);
                }
                if (isSuccess) {
                    log.warn("[Notice] Find uncaught exception, waiting for retry. ", t);
                } else {
                    throw new RuntimeException(t);
                }
            }
        }

        List<Runnable> listFailure() {
            if (CollectionUtils.isEmpty(failureQueue)) {
                return Lists.newArrayList();
            }

            List<Runnable> failureList = Lists.newArrayList();
            synchronized (LOCK) {
                while (!failureQueue.isEmpty()) {
                    failureList.add(failureQueue.poll());
                }
            }
            return failureList;
        }

        void clearFailure() {
            synchronized (LOCK) {
                failureQueue.clear();
            }
        }

    }

}
