package com.ecommerce.common.aspect;

import com.ecommerce.common.exception.BusinessException;
import com.ecommerce.common.response.CommonResultCode;
import com.ecommerce.common.security.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class AdminOnlyAspect {
    
    @Around("@annotation(com.ecommerce.common.annotation.AdminOnly) || @within(com.ecommerce.common.annotation.AdminOnly)")
    public Object checkAdminAccess(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!SecurityUtils.isAdmin()) {
            String userId = SecurityUtils.getCurrentUserId();
            String role = SecurityUtils.getCurrentUserRole();
            
            log.warn("Admin access denied: userId={}, role={}, method={}", 
                    userId, role, joinPoint.getSignature().getName());
            
            throw new BusinessException(CommonResultCode.FORBIDDEN);
        }
        
        return joinPoint.proceed();
    }
}