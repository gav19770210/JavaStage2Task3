package ru.gav19770210.stage2task3;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheCleanerConfig {
    CacheCleanerType cacheCleanerType() default CacheCleanerType.GET_VALUE;
}
