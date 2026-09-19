package com.softdreams.activityhub.anotation;

import java.lang.annotation.*;

import com.softdreams.activityhub.enums.EventType;
import com.softdreams.activityhub.enums.TargetType;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ActivityLog {

    EventType eventType();

    TargetType targetType();

    /**
     * SpEL expression evaluated after the method returns successfully.
     * Available: {@code #result} (the method's return value), every method
     * parameter by its name (e.g. {@code #userId}, {@code #request}), and the
     * {@code #jwtSubject(token)} helper to pull a username out of a JWT.
     */
    String targetId() default "#result.id";
}
