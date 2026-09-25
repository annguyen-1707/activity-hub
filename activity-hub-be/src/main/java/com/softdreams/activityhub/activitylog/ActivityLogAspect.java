package com.softdreams.activityhub.activitylog;

import java.lang.reflect.Method;
import java.text.ParseException;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import com.nimbusds.jwt.SignedJWT;
import com.softdreams.activityhub.anotation.ActivityLog;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class ActivityLogAspect {

    private final ActivityLogService activityLogService;

    private final ExpressionParser parser = new SpelExpressionParser();

    private static final Method JWT_SUBJECT_FUNCTION = resolveJwtSubjectMethod();

    // around để nghiệp vụ chính chạy thành công ms lưu log
    @Around("@annotation(activityLog)")
    public Object logActivity(
            ProceedingJoinPoint joinPoint,
            ActivityLog activityLog
    ) throws Throwable {

        // 1. Chạy nghiệp vụ chính
        Object result = joinPoint.proceed();

        // 2. Chỉ tạo log nếu nghiệp vụ thành công
        String targetId = resolveTargetId(
                joinPoint,
                activityLog,
                result
        );

        // 3. Lưu OutboxEvent
        activityLogService.log(
                activityLog.eventType(),
                activityLog.targetType(),
                targetId
        );

        // 4. Trả kết quả nghiệp vụ
        return result;
    }

    private String resolveTargetId(JoinPoint joinPoint, ActivityLog activityLog, Object result) {
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("result", result);
        context.registerFunction("jwtSubject", JWT_SUBJECT_FUNCTION);
        bindMethodArguments(joinPoint, context);

        Object value = parser.parseExpression(activityLog.targetId()).getValue(context);

        return value != null ? value.toString() : null;
    }

    private void bindMethodArguments(JoinPoint joinPoint, StandardEvaluationContext context) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] paramNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        if (paramNames == null) {
            return;
        }

        for (int i = 0; i < paramNames.length && i < args.length; i++) {
            context.setVariable(paramNames[i], args[i]);
        }
    }

    /**
     * Pulls the "sub" claim out of a JWT without verifying it, so login/logout
     * events (which carry no entity id) can still be tied to a username. It is
     * used only for labeling the log entry, never for authentication.
     */
    public static String extractJwtSubject(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }

        try {
            return SignedJWT.parse(token).getJWTClaimsSet().getSubject();
        } catch (ParseException e) {
            return null;
        }
    }

    private static Method resolveJwtSubjectMethod() {
        try {
            return ActivityLogAspect.class.getDeclaredMethod("extractJwtSubject", String.class);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }
}
