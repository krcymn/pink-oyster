package com.maplewood.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class EnrollmentLoggingAspect {

    @Around("execution(* com.maplewood.service.EnrollmentService.*(..))")
    public Object logEnrollmentOperations(ProceedingJoinPoint joinPoint) throws Throwable {
        String method = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        long start = System.currentTimeMillis();

        log.info("[ENROLLMENT] {} started — args: {}", method, args);

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - start;
            log.info("[ENROLLMENT] {} completed in {}ms", method, duration);
            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - start;
            log.warn("[ENROLLMENT] {} failed in {}ms — reason: {}", method, duration, e.getMessage());
            throw e;
        }
    }
}
