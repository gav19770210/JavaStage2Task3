package ru.gav19770210.stage2task3;

/**
 * Класс <b>CacheValue</b> описывает значение в хранилище кэшируемых данных.
 */
final class CacheValue {
    /**
     * Срок жизни кэшированного значения.
     */
    private final long expirePeriod;
    /**
     * Кэшированное значение.
     */
    private Object value;
    /**
     * Время помещения значения в кэш или его обновления в кэше.
     */
    private long cacheTime;

    public CacheValue(Object value, long cacheTime, long expirePeriod) {
        this.value = value;
        this.cacheTime = cacheTime;
        this.expirePeriod = expirePeriod;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public void setCacheTime(long cacheTime) {
        this.cacheTime = cacheTime;
    }

    public long getExpirePeriod() {
        return expirePeriod;
    }

    /**
     * Проверка, истёк ли срок жизни кэшированного значения или нет.
     *
     * @param currentTime время, на которое выполняется проверка
     * @return true - срок жизни кэшированного значения истёк, иначе false
     */
    public boolean isExpire(long currentTime) {
        return this.expirePeriod == 0 && currentTime == 0 || this.expirePeriod > 0
                && currentTime > 0 && this.cacheTime + this.expirePeriod < currentTime;
    }
}
