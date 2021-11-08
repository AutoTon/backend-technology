package com.technology.retry;

public interface IExecutor<T> {

    /**
     * 执行业务
     * @return T
     * @throws Exception
     */
    T execute() throws Exception;

}
