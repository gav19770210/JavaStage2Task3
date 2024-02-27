package ru.gav19770210.stage2task3;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Класс <b>CacheStore</b> реализует хранение кэшируемых данных.
 */
final class CacheStore {
    /**
     * Коллекция в разрезе методов проксируемого объекта для хранения кэшированных данных по уникальному ключу.
     */
    private final Map<Method, Map<CacheKey, CacheValue>> cacheValues = new ConcurrentHashMap<>();
    /**
     * Минимальный срок жизни кэшированных значений в хранилище.
     */
    private long minExpirePeriod = Long.MAX_VALUE;

    /**
     * В конструкторе выполняется первоначальное заполнение коллекции <b>cacheValues</b>
     * методами проксируемого объекта, помеченными аннотацией <b>@Cache</b>.
     *
     * @param object проксируемый объект
     */
    public CacheStore(Object object) {
        Arrays.stream(object.getClass().getMethods())
                .filter(method -> method.isAnnotationPresent(Cache.class))
                .forEach(method -> cacheValues.put(method, new ConcurrentHashMap<>()));
    }

    public long getMinExpirePeriod() {
        return minExpirePeriod;
    }

    /**
     * Получение значения из кэша по ключевым параметрам.
     *
     * @param method   проксируемый метод
     * @param cacheKey уникальный ключ, идентифицирующий значение
     * @return объект кэшированного значения
     */
    public CacheValue getValue(Method method, CacheKey cacheKey) {
        return this.cacheValues.get(method).get(cacheKey);
    }

    /**
     * Добавлене значения в кэш.
     *
     * @param method     проксируемый метод
     * @param cacheKey   уникальный ключ, идентифицирующий значение
     * @param cacheValue объект кэшированного значения
     */
    public void putValue(Method method, CacheKey cacheKey, CacheValue cacheValue) {
        this.cacheValues.get(method).put(cacheKey, cacheValue);
        if (cacheValue.getExpirePeriod() != 0) {
            minExpirePeriod = Long.min(minExpirePeriod, cacheValue.getExpirePeriod());
        }
    }

    /**
     * Очистка кэшированных данных с истёкшим сроком жизни.
     *
     * @param expireTime время, на которое выполняется проверка
     */
    public void cleanExpireCache(long expireTime) {
        for (var cacheMethod : this.cacheValues.values()) {
            cleanExpireCacheMethod(cacheMethod, expireTime);
        }
    }

    /**
     * Очистка кэшированных данных с истёкшим сроком жизни для заданного метода.
     *
     * @param method     метод, по которому выполняется анализ кэшированных данных
     * @param expireTime время, на которое выполняется проверка
     */
    public void cleanExpireCacheForMethod(Method method, long expireTime) {
        var cacheMethod = this.cacheValues.get(method);
        if (Objects.nonNull(cacheMethod)) {
            cleanExpireCacheMethod(cacheMethod, expireTime);
        }
    }

    private void cleanExpireCacheMethod(Map<CacheKey, CacheValue> cacheMethod, long expireTime) {
        for (var cacheValueEntry : cacheMethod.entrySet()) {
            if (cacheValueEntry.getValue().isExpire(expireTime)) {
                cacheMethod.remove(cacheValueEntry.getKey());
            }
        }
    }
}
