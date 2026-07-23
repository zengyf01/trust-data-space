package com.tds.common.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * 操作日志切面 - 自动记录操作日志
 */
@Aspect
@Component
@Order(1)
public class OperationLogAspect {

    private static final Logger logger = LoggerFactory.getLogger(OperationLogAspect.class);

    @Pointcut("@annotation(com.tds.common.aspect.OperationLog)")
    public void logPointcut() {}

    @Around("logPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        // 获取请求信息
        String url = "";
        String method = "";
        String ipAddress = "";
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                url = request.getRequestURI();
                method = request.getMethod();
                ipAddress = getClientIp(request);
            }
        } catch (Exception e) {
            logger.debug("获取请求信息失败", e);
        }

        // 获取方法信息
        String module = "";
        String operation = "";
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method methodObj = signature.getMethod();
            OperationLog annotation = methodObj.getAnnotation(OperationLog.class);
            if (annotation != null) {
                module = annotation.module();
                operation = annotation.operation();
            }
        } catch (Exception e) {
            logger.debug("获取方法信息失败", e);
        }

        Object result = null;
        int status = 1;
        String errorMessage = null;
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Exception e) {
            status = 2;
            errorMessage = e.getMessage();
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            // 日志记录（简化版，不依赖其他模块）
            logger.info("操作日志: module={}, operation={}, url={}, method={}, ip={}, duration={}ms, status={}",
                    module, operation, url, method, ipAddress, duration, status);
            if (errorMessage != null) {
                logger.error("操作失败: {}", errorMessage);
            }
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String[] headers = {
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP"
        };
        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr();
    }

    private String getRequestParams(ProceedingJoinPoint joinPoint) {
        try {
            Object[] args = joinPoint.getArgs();
            if (args == null || args.length == 0) {
                return "";
            }
            return truncateString(Arrays.toString(args), 2000);
        } catch (Exception e) {
            return "";
        }
    }

    private String truncateString(String str, int maxLength) {
        if (str == null) {
            return "";
        }
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength);
    }
}