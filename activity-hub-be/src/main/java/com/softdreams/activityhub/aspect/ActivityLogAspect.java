package com.softdreams.activityhub.aspect;

import com.softdreams.activityhub.anotation.ActivityLog;
import com.softdreams.activityhub.service.ActivityLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class ActivityLogAspect {

    private final ActivityLogService activityLogService;

    private final ExpressionParser parser =
            new SpelExpressionParser();

    @AfterReturning(
            pointcut = "@annotation(activityLog)",
            returning = "result"
    )
    public void logActivity(
            ActivityLog activityLog,
            Object result
    ) {

        StandardEvaluationContext context =
                new StandardEvaluationContext();

        context.setVariable("result", result);

        Object value = parser
                .parseExpression(activityLog.targetId())
                .getValue(context);

        String targetId = value != null
                ? value.toString()
                : null;

        activityLogService.log(
                activityLog.eventType(),
                activityLog.targetType(),
                targetId
        );
    }
}
