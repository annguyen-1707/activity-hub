package com.softdreams.activityhub.anotation;

import com.softdreams.activityhub.enums.EventType;
import com.softdreams.activityhub.enums.TargetType;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ActivityLog {

    EventType eventType();

    TargetType targetType();

    String targetId() default "#result.id";

}