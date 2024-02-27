package ru.gav19770210.stage2task3;

import java.util.Arrays;
import java.util.Objects;

public class CacheUtils {
    /**
     * Проверка, имеет ли исходный объект методы с аннотацией <b>@Cache</b>
     *
     * @param object    исходный объект
     * @return  true - исходный объект имеет методы с аннотацией <b>@Cache</b>, иначе false
     */
    public static boolean objectIsCacheable(Object object) {
        return Arrays.stream(object.getClass().getMethods())
                .anyMatch(method -> method.isAnnotationPresent(Cache.class));
    }

    /**
     * Проверка, имеет ли исходный объект методы с аннотацией <b>@Cache</b> и не нулевое значение срока жизни.
     *
     * @param object    исходный объект
     * @return  true - исходный объект имеет методы с аннотацией <b>@Cache</b>, иначе false
     */
    public static boolean objectIsCacheableWithExpirePeriod(Object object) {
        return Arrays.stream(object.getClass().getMethods())
                .filter(method -> method.isAnnotationPresent(Cache.class))
                .anyMatch(method -> method.getAnnotation(Cache.class).expirePeriod() > 0);
    }

    /**
     * Получение типа механизма очистки кэшированных данных исходного объекта.
     *
     * @param object    исходный объект
     * @return  тип механизма очистки кэшированных данных
     */
    public static CacheCleanerType getCacheCleanerType(Object object) {
        var cacheCleanerConfig = object.getClass().getAnnotation(CacheCleanerConfig.class);
        return Objects.nonNull(cacheCleanerConfig) ? cacheCleanerConfig.cacheCleanerType() : CacheCleanerType.GET_VALUE;
    }
}
