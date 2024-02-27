package ru.gav19770210.stage2task3;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
/*
 * Аннотация для исключения полей класса из тестирования.
 */
public @interface CacheTest {
}
