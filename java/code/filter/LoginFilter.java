package com.technology.filter;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import lombok.extern.log4j.Log4j2;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * IP频次限制-登录
 */
@Log4j2
public class LoginFilter implements Filter {

    /**
     * 3秒/次
     */
    private static final int DURATION = 3000;
    private static final String PREFIX = "LOGIN_IP_";

    private static final Cache<String, List<Long>> LOGIN_IP_CACHE = CacheBuilder.newBuilder()
            .initialCapacity(100)
            .expireAfterWrite(10, TimeUnit.DAYS)
            .build();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String ip = IPUtil.getRealIp(request);
        if (isExceed(ip)) {
            HttpServletResponse response = (HttpServletResponse) servletResponse;
            response.setStatus(406);
            response.setHeader("Cache-Control", "no-store");
            response.setDateHeader("Expires", 0);
            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().println("您的请求次数已超限，请在3秒后重试");
            response.getWriter().flush();
        } else {
            chain.doFilter(servletRequest, servletResponse);
        }
    }

    /**
     * 检查登录IP是否超限
     * @return true - 超限
     */
    private boolean isExceed(String ip) {
        if (log.isDebugEnabled()) {
            log.debug("[Login] remote ip=[{}]. ", ip);
        }

        boolean isExceed = false;
        String key = PREFIX + ip;
        synchronized (key.intern()) {
            List<Long> list = clearAndGet(key);
            LOGIN_IP_CACHE.put(key, list);
            if (list.size() > 1) {
                log.warn("[Login] remote ip exceed limit, ip=[{}]. ", ip);
                isExceed = true;
            }
        }
        return isExceed;
    }

    private List<Long> clearAndGet(String key) {
        List<Long> list = LOGIN_IP_CACHE.getIfPresent(key);
        if (list == null) {
            list = Lists.newArrayList();
        }

        long current = System.currentTimeMillis();
        list = list.stream().filter(time -> current - time < DURATION).collect(Collectors.toList());
        list.add(current);
        return list;
    }

}
