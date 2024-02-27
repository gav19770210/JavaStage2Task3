package ru.gav19770210.stage2task3;

/**
 * Тип механизма очистки кэшированных данных.
 */
public enum CacheCleanerType {
    GET_VALUE,  // очистка кэша при получении значения
    BACKGROUND  // очистка кэша сборщиком данных с истёкшим сроком жизни
}
