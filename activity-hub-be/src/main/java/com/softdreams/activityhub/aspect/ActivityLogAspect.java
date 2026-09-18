package com.softdreams.activityhub.aspect;

import java.lang.reflect.Method;
import java.text.ParseException;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import com.nimbusds.jwt.SignedJWT;
import com.softdreams.activityhub.anotation.ActivityLog;
import com.softdreams.activityhub.service.ActivityLogService;

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

    @AfterReturning(pointcut = "@annotation(activityLog)", returning = "result")
    public void logActivity(JoinPoint joinPoint, ActivityLog activityLog, Object result) {
        try {
            String targetId = resolveTargetId(joinPoint, activityLog, result);

            activityLogService.log(activityLog.eventType(), activityLog.targetType(), targetId);
        } catch (Exception e) {
            // Auditing must never break the business flow it observes.
            log.warn(
                    "Skipped activity log for {} on {}: {}",
                    activityLog.eventType(),
                    joinPoint.getSignature().toShortString(),
                    e.getMessage());
        }
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
