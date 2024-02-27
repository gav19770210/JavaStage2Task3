package ru.gav19770210.stage2task3;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Класс <b>CacheKey</b> описывает уникальный ключ, идентифицирующий значение кэша.
 */
final class CacheKey {
    private final Object[] parameters;

    private final Map<Field, Object> stateFields;

    public CacheKey(Object[] parameters, Object object) throws IllegalAccessException {
        this.parameters = parameters;
        this.stateFields = getStateFields(object);
    }

    private Map<Field, Object> getStateFields(Object object) throws IllegalAccessException {
        Map<Field, Object> cacheFields = new HashMap<>();
        var objectClass = object.getClass();
        do {
            for (var field : objectClass.getDeclaredFields()) {
                if (!field.isAnnotationPresent(CacheTest.class)) {
                    field.setAccessible(true);
                    cacheFields.put(field, field.get(object));
                }
            }
            objectClass = objectClass.getSuperclass();
        } while (objectClass != Object.class);

        return cacheFields;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof CacheKey cacheKey)) return false;
        return Arrays.equals(parameters, cacheKey.parameters) && Objects.equals(stateFields, cacheKey.stateFields);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(stateFields);
        result = 31 * result + Arrays.hashCode(parameters);
        return result;
    }
}
