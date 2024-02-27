package ru.gav19770210.stage2task3;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Сборщик кэшированных данных с истёкшим сроком жизни.
 * Очистка автоматически запускается при добавлении первого хранилища кэшированных данных.
 * Стратегия очистки - интеравальное выполнение.
 * Длина интервала определяется как минимальное время жизни объектов в очищаемых хранилищах кэшированных данных.
 * При этом, каждый из очищаемых хранилищ чистится не чаще чем минимальное время жизни его объектов.
 */
public class CacheCleaner {
    /**
     * Количество миллисекунд, по умолчанию, сна между циклами очистки кэшированных данных.
     */
    private final static int DURATION_SLEEP_CACHE_CLEANER_IN_MILLISECONDS = 1000;
    /**
     * Список хранилищ кэшированных данных прокси-объектов.
     */
    private final List<CacheStoreItem> cacheStorages;
    /**
     * Поток сборщика кэшированных данных.
     */
    private final CacheCleanerTask cacheCleanerThread;
    /**
     * Интерфейс получения текущего времени.
     */
    private Clockable clock;
    /**
     * Минимальный срок жизни кэшированных значений в хранилище,
     * для расчёта минимального периода запуска цикла очистки.
     */
    private long minExpirePeriod = Long.MAX_VALUE;

    public CacheCleaner(Clockable clock) {
        this.cacheStorages = new ArrayList<>();
        this.cacheCleanerThread = getCacheCleanerThread();
        this.clock = clock;
    }

    public void setClock(Clockable clock) {
        this.clock = clock;
    }

    private CacheCleanerTask getCacheCleanerThread() {
        if (cacheCleanerThread == null) {
            var thread = new CacheCleanerTask();
            thread.setDaemon(true);
            return thread;
        }
        return cacheCleanerThread;
    }

    /**
     * Добавление хранилища кэшированных данных в коллекцию сборщика.
     *
     * @param cacheStore хранилище кэшированных данных
     */
    public void addCacheStorage(CacheStore cacheStore) {
        cacheStorages.add(new CacheStoreItem(cacheStore));
        minExpirePeriod = Long.min(minExpirePeriod, cacheStore.getMinExpirePeriod());
    }

    /**
     * Запуск потока сборщика кэшированных данных.
     */
    public void startClean() {
        if (!cacheCleanerThread.isInterrupted()) {
            if (!cacheCleanerThread.isAlive()) {
                cacheCleanerThread.start();
            }
        }
    }

    /**
     * Освобождение коллекции хранилищ кэшированных данных.
     */
    public void clearCacheStorages() {
        cacheStorages.clear();
    }

    /**
     * Очистка кэшированных данных с истёкшим сроком жизни.
     *
     * @param cacheStoreItem контейнер хранилища кэшированных данных
     */
    private void cleanCacheStore(CacheStoreItem cacheStoreItem) {
        var verifyTime = clock.currentTimeMillis();
        if (verifyTime - cacheStoreItem.lastTimeProcessed >= cacheStoreItem.cacheStore.getMinExpirePeriod()) {
            cacheStoreItem.cacheStore.cleanExpireCache(verifyTime);
            cacheStoreItem.lastTimeProcessed = verifyTime;
        }
        minExpirePeriod = Long.min(minExpirePeriod, cacheStoreItem.cacheStore.getMinExpirePeriod());
    }

    /**
     * Задача потока сборщика кэшированных данных.
     */
    private final class CacheCleanerTask extends Thread {

        @Override
        public void run() {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    // Засекаем старт очистки
                    var startClearing = clock.currentTimeMillis();

                    cacheStorages.iterator().forEachRemaining(CacheCleaner.this::cleanCacheStore);

                    // определяем период сна, с корректировкой на время затраченное на выполнение очистки
                    long timeToSleep;
                    if (minExpirePeriod == Long.MAX_VALUE) {
                        timeToSleep = DURATION_SLEEP_CACHE_CLEANER_IN_MILLISECONDS;
                    } else {
                        timeToSleep = minExpirePeriod;
                    }
                    timeToSleep = timeToSleep - (clock.currentTimeMillis() - startClearing);

                    // засыпаем
                    if (timeToSleep > 0) {
                        TimeUnit.MILLISECONDS.sleep(timeToSleep);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Контейнер для хранения информации о хранилище кэшированных данных.
     */
    class CacheStoreItem {
        /**
         * Хранилище кэшированных данных.
         */
        private final CacheStore cacheStore;
        /**
         * Время последнего анализа кэшированных данных.
         */
        private long lastTimeProcessed;

        public CacheStoreItem(CacheStore cacheStore) {
            this.cacheStore = cacheStore;
            this.lastTimeProcessed = clock.currentTimeMillis();
        }
    }
}
