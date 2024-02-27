package ru.gav19770210.stage2task3;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Класс <b>CacheInvocationHandler</b> перехватывает вызовы методов интерфейса прокси-объекта.
 * Реализует логику кэширования вызовов методов, помеченных аннотацией <b>@Cache</b>.
 * Выполняет перевызов интерфейсных методов, помеченных аннотацией <b>@Cache</b> исходного объекта <b>cachedObject</b>
 * только в том случае, если это первый вызов метода или срок жизни результата вызова метода истёк.
 * Срок жизни результата вызова метода определяется параметром <b>expirePeriod</b> в аннотации <b>@Cache</b>.
 * Если срок жизни не задан, или задан нулевым,
 * то срок жизни определяется фиксацией вызовов интерфейсных методов, помеченных аннотацией <b>@Mutator</b>.
 * Остальные интерфейсные методы перевызываются на исходном объекте без изменения логики работы.
 */
final class CacheInvocationHandler implements InvocationHandler {
    /**
     * Сборщик кэшированных данных с истёкшим сроком жизни.
     */
    private static CacheCleaner cacheCleaner;
    /**
     * Проксируемый объект.
     */
    private final Object cachedObject;
    /**
     * Коллекция для хранения соответствия методов прокси-объекта и проксируемого объекта,
     * чтобы каждый раз не выполнять поиск их соответствия через java.lang.reflect.
     */
    private final Map<Method, CachedObjectMethod> cachedObjectMethods;
    /**
     * Хранилище кэшированных данных.
     */
    private final CacheStore cacheStore;
    /**
     * Интерфейс получения текущего времени.
     */
    private final Clockable clock;
    /**
     * Тип механизма очистки кэшированных данных.
     */
    private final CacheCleanerType cachedObjectCleanerType;

    /**
     * В конструкторе выполняется первоначальное создание сборщика кэшированных данных если,
     * на проксируемом объекте задан тип очистка кэша через сборщик кэшированных данных и
     * у проксирумого объекта есть методы с не нелувым сроком жизни.
     *
     * @param cachedObject  проксируемый объект
     * @param clock     интерфейс получения текущего времени
     */
    public CacheInvocationHandler(Object cachedObject, Clockable clock) {
        this.cachedObject = cachedObject;
        this.cachedObjectMethods = new HashMap<>();
        this.cacheStore = new CacheStore(cachedObject);
        this.clock = clock;
        this.cachedObjectCleanerType = CacheUtils.getCacheCleanerType(cachedObject);

        if (this.cachedObjectCleanerType == CacheCleanerType.BACKGROUND
                && CacheUtils.objectIsCacheableWithExpirePeriod(cachedObject)) {
            if (cacheCleaner == null) {
                cacheCleaner = new CacheCleaner(this.clock);
            }
            cacheCleaner.addCacheStorage(this.cacheStore);
            cacheCleaner.startClean();
        }
    }

    public static CacheCleaner getCacheCleaner() {
        return cacheCleaner;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        CachedObjectMethod cachedObjectMethod = getCachedObjectMethod(method);
        if (cachedObjectMethod != null) {
            Object result;
            if (cachedObjectMethod.isCache()) {
                var cacheKey = new CacheKey(args, cachedObject);
                var cacheValue = cacheStore.getValue(cachedObjectMethod.getMethod(), cacheKey);

                if (Objects.nonNull(cacheValue)) {
                    if (cacheValue.isExpire(clock.currentTimeMillis())) {
                        result = method.invoke(cachedObject, args);
                        cacheValue.setValue(result);
                    } else {
                        result = cacheValue.getValue();
                    }
                    cacheValue.setCacheTime(clock.currentTimeMillis());
                } else {
                    result = method.invoke(cachedObject, args);
                    cacheValue = new CacheValue(result, clock.currentTimeMillis(), cachedObjectMethod.getExpirePeriod());
                    cacheStore.putValue(cachedObjectMethod.getMethod(), cacheKey, cacheValue);
                }
                /*
                 * Очистка кэша по текущему методу
                 */
                if (this.cachedObjectCleanerType == CacheCleanerType.GET_VALUE) {
                    cacheStore.cleanExpireCacheForMethod(cachedObjectMethod.getMethod(), clock.currentTimeMillis());
                }
            } else {
                /*
                 * Очистка кэша по методам с не заданным временем жизни т.е. равным 0
                 */
                if (cachedObjectMethod.isMutator()) {
                    cacheStore.cleanExpireCache(0);
                }
                result = method.invoke(cachedObject, args);
            }
            return result;
        } else {
            return null;
        }
    }

    /**
     * Функция <b>getCachedObjectMethod</b> по методу прокси-объекта,
     * возвращает соответствующий ему метод проксируемого объекта.
     * <p>
     * Результат поиска сохраняется в коллекцию <b>cachedObjectMethods</b>.
     *
     * @param method метод прокси-объекта
     */
    private CachedObjectMethod getCachedObjectMethod(Method method) {
        CachedObjectMethod cachedObjectMethod = null;

        if (cachedObjectMethods.containsKey(method)) {
            cachedObjectMethod = cachedObjectMethods.get(method);
        } else {
            try {
                var cachedMethod = cachedObject.getClass().getMethod(method.getName(), method.getParameterTypes());
                cachedObjectMethod = new CachedObjectMethod(cachedMethod);
            } catch (NoSuchMethodException ignored) {
            }
            cachedObjectMethods.put(method, cachedObjectMethod);
        }
        return cachedObjectMethod;
    }

    /**
     * Класс <b>CachedObjectMethod</b> для хранения необходимых настроек аннотаций метода проксируемого объекта.
     */
    static class CachedObjectMethod {
        private final Method method;
        private final boolean isCache;
        private final boolean isMutator;
        private final long expirePeriod;

        public CachedObjectMethod(Method method) {
            this.method = method;
            this.isCache = method.isAnnotationPresent(Cache.class);
            if (this.isCache) {
                this.isMutator = false;
                expirePeriod = method.getAnnotation(Cache.class).expirePeriod();
            } else {
                this.isMutator = method.isAnnotationPresent(Mutator.class);
                expirePeriod = 0;
            }
        }

        public Method getMethod() {
            return method;
        }

        public boolean isCache() {
            return isCache;
        }

        public boolean isMutator() {
            return isMutator;
        }

        public long getExpirePeriod() {
            return expirePeriod;
        }
    }
}
