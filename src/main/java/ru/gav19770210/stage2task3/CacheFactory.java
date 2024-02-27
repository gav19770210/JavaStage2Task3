package ru.gav19770210.stage2task3;

import java.lang.reflect.Proxy;

/**
 * Класс <b>CacheFactory</b> реализует методы создания прокси-объекта для исходного объекта.
 * Прокси-объект создаётся если исходный объект имеет методы с аннотацией <b>@Cache</b>,
 * иначе возвращается исходный объект.
 */
public final class CacheFactory {
    /**
     * Создание прокси-объекта для исходного объекта.
     *
     * @param cachedObject исходный объект
     * @return прокси-объект
     */
    public static Object makeCacheable(Object cachedObject) {
        return makeCacheable(cachedObject, System::currentTimeMillis);
    }

    /**
     * Создание прокси-объекта для исходного объекта.
     *
     * @param cachedObject исходный объект
     * @param clock        интерфейс получения текущего времени
     * @return прокси-объект
     */
    public static Object makeCacheable(Object cachedObject, Clockable clock) {
        if (CacheUtils.objectIsCacheable(cachedObject)) {
            return Proxy.newProxyInstance(cachedObject.getClass().getClassLoader(),
                    cachedObject.getClass().getInterfaces(),
                    new CacheInvocationHandler(cachedObject, clock));
        } else {
            return cachedObject;
        }
    }

    /**
     * Если сборщик кэшированных данных создан, то освобождение хранилищ кэшированных данных.
     */
    public static void clearCacheStorages() {
        if (CacheInvocationHandler.getCacheCleaner() != null) {
            CacheInvocationHandler.getCacheCleaner().clearCacheStorages();
        }
    }
}
