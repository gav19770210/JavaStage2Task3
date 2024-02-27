package ru.gav19770210.stage2task3;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Cache {
    /**
     *  Срок жизни объекта в кэше в миллисекундах.
     *  <p>Если срок жизни задан 0, то изменение объекта определяется фиксацией вызовов интерфейсных методов,
     *  помеченных аннотацией <b>@Mutator</b>.
     */
    long expirePeriod() default 0;
}
