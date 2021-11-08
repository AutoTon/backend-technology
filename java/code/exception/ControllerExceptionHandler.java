package com.technology.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;

/**
 * controller异常拦截处理
 */
@Slf4j
@RestControllerAdvice(basePackages = "com.xxx")
public class ControllerExceptionHandler {

    private static final String SERVER_ERROR = "服务器异常...";
    private static final String PARAM_ERROR = "参数错误，请检查参数及其类型是否合法";

    /**
     * 方法参数校验
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public BizResult handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.warn("MethodArgumentNotValidException param error", e);
        String message = e.getBindingResult().getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(","));
        return BizResult.failResult(message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public BizResult handleHttpMessageNotReadableException(HttpServletRequest request, HttpMessageNotReadableException e) {
        log.warn("[Controller] Find HttpMessageNotReadableException, uri=[{}]. ", request.getRequestURI(), e);
        return BizResult.failResult(PARAM_ERROR);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public BizResult handleIllegalArgumentException(HttpServletRequest request, IllegalArgumentException e) {
        log.warn("[Controller] Find IllegalArgumentException, uri=[{}]. ", request.getRequestURI(), e);
        return BizResult.failResult(e.getMessage());
    }

    @ExceptionHandler(CommonException.class)
    public BizResult handleCommonException(HttpServletRequest request, CommonException e) {
        log.info("[Controller] Find CommonException, uri=[{}]. ", request.getRequestURI(), e);
        BizResult result = BizResult.failResult(e.getMsg());
        result.setCode(Math.toIntExact(e.getCode()));
        return result;
    }

    @ExceptionHandler(Exception.class)
    public BizResult handleException(HttpServletRequest request, Exception e) {
        log.error("[Controller] Find Exception, uri=[{}]. ", request.getRequestURI(), e);
        return BizResult.failResult(SERVER_ERROR);
    }

}
