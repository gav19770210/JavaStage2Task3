package ru.gav19770210.stage2task3;

import org.junit.jupiter.api.*;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class CacheTests {
    private final TestClock testClock = new TestClock(1L);
    private final TestClockBlock testClockBlock = new TestClockBlock();
    private TestCacheableClass testCacheableObject;
    private TestCacheable testCacheableProxy;

    @BeforeEach
    public void testBeforeEach() {
        System.out.println("Создаём объект testCacheableObject, который будем кешировать");
        testCacheableObject = new TestCacheableClass(10);
        System.out.println("Создаём прокси-объект testCacheableProxy для тестового кэшируемого объекта\n");
        Assertions.assertDoesNotThrow(() -> {
            testCacheableProxy = (TestCacheable) CacheFactory.makeCacheable(testCacheableObject, testClock);
        }, "Не удалось создать прокси-объект для кэшируемого объекта testCacheableObject");
    }

    @AfterEach
    public void testAfterEach() {
        System.out.println("Освобождение коллекции хранилищ кэшированных данных");
        CacheFactory.clearCacheStorages();
        testCacheableProxy = null;
        testCacheableObject = null;
    }

    @DisplayName("Двойной вызыв кэшируемого метода без истечения срока жизни кэшируемых данных")
    @Test
    public void testCacheableMethodWithoutExpireV1() {
        System.out.println("-> Двойной вызыв кэшируемого метода cacheableMethod сразу после создания прокси-объекта с начальными значениями\n");

        System.out.println("Вызываем на прокси-объекте кэшируемый метод cacheableMethod");
        var resultCacheableMethod = testCacheableProxy.cacheableMethod();
        System.out.println("Проверяем, что кешируемый метод cacheableMethod был вызван");
        Assertions.assertEquals(testCacheableObject.callDoubleValueCount, 1, "Вызов cacheableMethod №1 НЕ был выполнен");
        System.out.println("Проверяем, что кешируемый метод cacheableMethod вернул верное значение");
        Assertions.assertEquals(resultCacheableMethod, 100, "Вызов cacheableMethod №1 вернул НЕ верное значение");
        System.out.println();

        System.out.println("Вызываем на прокси-объекте кэшируемый метод cacheableMethod второй раз");
        var resultCacheableMethod2 = testCacheableProxy.cacheableMethod();
        System.out.println("Проверяем, что кешируемый метод cacheableMethod НЕ был вызван");
        Assertions.assertEquals(testCacheableObject.callDoubleValueCount, 1, "Вызов cacheableMethod №2 был выполнен");
        System.out.println("Проверяем, что кешируемый метод cacheableMethod вернул верное значение");
        Assertions.assertEquals(resultCacheableMethod2, 100, "Вызов cacheableMethod №2 вернул НЕ верное значение");
        System.out.println();

        System.out.println("-> Двойной вызыв кэшируемого метода cacheableMethod после вызова метода мутатора, который меняет состояние объекта\n");
        System.out.println("Вызываем на прокси-объекте метод-мутатор mutatorMethod, который меняет состояние объекта\n");
        testCacheableProxy.mutatorMethod(20);

        System.out.println("Вызываем на прокси-объекте кэшируемый метод cacheableMethod");
        resultCacheableMethod = testCacheableProxy.cacheableMethod();
        System.out.println("Проверяем, что кешируемый метод cacheableMethod был вызван");
        Assertions.assertEquals(testCacheableObject.callDoubleValueCount, 1, "Вызов cacheableMethod №1 НЕ был выполнен");
        System.out.println("Проверяем, что кешируемый метод cacheableMethod вернул верное значение");
        Assertions.assertEquals(resultCacheableMethod, 200, "Вызов cacheableMethod №1 вернул НЕ верное значение");
        System.out.println();

        System.out.println("Вызываем на прокси-объекте кэшируемый метод cacheableMethod второй раз");
        resultCacheableMethod2 = testCacheableProxy.cacheableMethod();
        System.out.println("Проверяем, что кешируемый метод cacheableMethod НЕ был вызван");
        Assertions.assertEquals(testCacheableObject.callDoubleValueCount, 1, "Вызов cacheableMethod №2 был выполнен");
        System.out.println("Проверяем, что кешируемый метод cacheableMethod вернул верное значение");
        Assertions.assertEquals(resultCacheableMethod2, 200, "Вызов cacheableMethod №2 вернул НЕ верное значение");
        System.out.println();

        System.out.println("-> Двойной вызыв кэшируемого метода cacheableMethod после вызова метода мутатора, который меняет состояние объекта к первоначальному\n");
        System.out.println("Вызываем на прокси-объекте метод-мутатор mutatorMethod, который меняет состояние объекта к первоначальному\n");
        testCacheableProxy.mutatorMethod(10);

        System.out.println("Вызываем на прокси-объекте кэшируемый метод cacheableMethod");
        resultCacheableMethod = testCacheableProxy.cacheableMethod();
        System.out.println("Проверяем, что кешируемый метод cacheableMethod НЕ был вызван");
        Assertions.assertEquals(testCacheableObject.callDoubleValueCount, 0, "Вызов cacheableMethod №1 был выполнен");
        System.out.println("Проверяем, что кешируемый метод cacheableMethod вернул верное значение");
        Assertions.assertEquals(resultCacheableMethod, 100, "Вызов cacheableMethod №1 вернул НЕ верное значение");
        System.out.println();

        System.out.println("Вызываем на прокси-объекте кэшируемый метод cacheableMethod второй раз");
        resultCacheableMethod2 = testCacheableProxy.cacheableMethod();
        System.out.println("Проверяем, что кешируемый метод cacheableMethod НЕ был вызван");
        Assertions.assertEquals(testCacheableObject.callDoubleValueCount, 0, "Вызов cacheableMethod №2 был выполнен");
        System.out.println("Проверяем, что кешируемый метод cacheableMethod вернул верное значение");
        Assertions.assertEquals(resultCacheableMethod2, 100, "Вызов cacheableMethod №2 вернул НЕ верное значение");
        System.out.println();

        System.out.println("-> Двойной вызыв кэшируемого метода cacheableMethod после вызова метода без аннотаций, который меняет состояние объекта\n");
        System.out.println("Вызываем на прокси-объекте метод без аннотаций unCacheableMethod, но который меняет состояние объекта\n");
        testCacheableProxy.unCacheableMethod();

        System.out.println("Вызываем на прокси-объекте кэшируемый метод cacheableMethod");
        resultCacheableMethod = testCacheableProxy.cacheableMethod();
        System.out.println("Проверяем, что кешируемый метод cacheableMethod был вызван");
        Assertions.assertEquals(testCacheableObject.callDoubleValueCount, 1, "Вызов cacheableMethod №1 НЕ был выполнен");
        System.out.println("Проверяем, что кешируемый метод cacheableMethod вернул верное значение");
        Assertions.assertEquals(resultCacheableMethod, 50, "Вызов cacheableMethod №1 вернул НЕ верное значение");
        System.out.println();

        System.out.println("Вызываем на прокси-объекте кэшируемый метод cacheableMethod второй раз");
        resultCacheableMethod2 = testCacheableProxy.cacheableMethod();
        System.out.println("Проверяем, что кешируемый метод cacheableMethod НЕ был вызван");
        Assertions.assertEquals(testCacheableObject.callDoubleValueCount, 1, "Вызов cacheableMethod №2 был выполнен");
        System.out.println("Проверяем, что кешируемый метод cacheableMethod вернул верное значение");
        Assertions.assertEquals(resultCacheableMethod2, 50, "Вызов cacheableMethod №2 вернул НЕ верное значение");
        System.out.println();
    }


    @DisplayName("Двойной вызыв кэшируемого метода с истечением срока жизни кэшируемых данных")
    @Test
    public void testCacheableMethodWithExpireV1() throws InterruptedException {
        System.out.println("-> Двойной вызыв кэшируемого метода cacheableMethod сразу после создания прокси-объекта с начальными значениями\n");

        System.out.println("Вызываем на прокси-объекте кэшируемый метод cacheableMethod");
        var resultCacheableMethod = testCacheableProxy.cacheableMethod();
        System.out.println("Проверяем, что кешируемый метод cacheableMethod был вызван");
        Assertions.assertEquals(testCacheableObject.callDoubleValueCount, 1, "Вызов cacheableMethod №1 НЕ был выполнен");
        System.out.println("Проверяем, что кешируемый метод cacheableMethod вернул верное значение");
        Assertions.assertEquals(resultCacheableMethod, 100, "Вызов cacheableMethod №1 вернул НЕ верное значение");
        System.out.println();

        System.out.println("Вызываем на прокси-объекте кэшируемый метод cacheableMethod второй раз");
        var resultCacheableMethod2 = testCacheableProxy.cacheableMethod();
        System.out.println("Проверяем, что кешируемый метод cacheableMethod НЕ был вызван");
        Assertions.assertEquals(testCacheableObject.callDoubleValueCount, 1, "Вызов cacheableMethod №2 был выполнен");
        System.out.println("Проверяем, что кешируемый метод cacheableMethod вернул верное значение");
        Assertions.assertEquals(resultCacheableMethod2, 100, "Вызов cacheableMethod №2 вернул НЕ верное значение");
        System.out.println();

        System.out.println("Пауза, чтобы истёк срок жизни кэшированных данных\n");
        //Thread.sleep(2000);
        testClock.time = 2000L;
        if (CacheUtils.getCacheCleanerType(testCacheableObject) == CacheCleanerType.BACKGROUND) {
            CacheInvocationHandler.getCacheCleaner().setClock(testClockBlock);
            testClockBlock.awaitConsume(2000L);
            testClockBlock.awaitConsume(2000L);
        }

        testCacheableObject.callDoubleValueCount = 0;

        System.out.println("Вызываем на прокси-объекте кэшируемый метод cacheableMethod");
        resultCacheableMethod = testCacheableProxy.cacheableMethod();
        System.out.println("Проверяем, что кешируемый метод cacheableMethod был вызван");
        Assertions.assertEquals(testCacheableObject.callDoubleValueCount, 1, "Вызов cacheableMethod №1 НЕ был выполнен");
        System.out.println("Проверяем, что кешируемый метод cacheableMethod вернул верное значение");
        Assertions.assertEquals(resultCacheableMethod, 100, "Вызов cacheableMethod №1 вернул НЕ верное значение");
        System.out.println();

        System.out.println("Вызываем на прокси-объекте кэшируемый метод cacheableMethod второй раз");
        resultCacheableMethod2 = testCacheableProxy.cacheableMethod();
        System.out.println("Проверяем, что кешируемый метод cacheableMethod НЕ был вызван");
        Assertions.assertEquals(testCacheableObject.callDoubleValueCount, 1, "Вызов cacheableMethod №2 был выполнен");
        System.out.println("Проверяем, что кешируемый метод cacheableMethod вернул верное значение");
        Assertions.assertEquals(resultCacheableMethod2, 100, "Вызов cacheableMethod №2 вернул НЕ верное значение");
        System.out.println();
    }

    interface TestCacheable {
        int cacheableMethod();

        void unCacheableMethod();

        void mutatorMethod(Object object);
    }

    static class TestClock implements Clockable {
        long time;

        public TestClock(long time) {
            this.time = time;
        }

        @Override
        public long currentTimeMillis() {
            return this.time;
        }
    }

    @CacheCleanerConfig(cacheCleanerType = CacheCleanerType.BACKGROUND)
    //@CacheCleanerConfig(cacheCleanerType = CacheCleanerType.GET_VALUE)
    static class TestCacheableClass implements TestCacheable {
        int intData;
        @CacheTest
        int callDoubleValueCount;

        public TestCacheableClass(int intData) {
            this.intData = intData;
        }

        @Override
        @Cache(expirePeriod = 1000)
        public int cacheableMethod() {
            callDoubleValueCount++;
            return intData * 10;
        }

        @Override
        public void unCacheableMethod() {
            intData = intData / 2;
            callDoubleValueCount = 0;
        }

        @Override
        @Mutator
        public void mutatorMethod(Object object) {
            intData = (Integer) object;
            callDoubleValueCount = 0;
        }
    }

    static class TestClockBlock implements Clockable {
        private final BlockingQueue<Long> time = new LinkedBlockingQueue<>();
        private final BlockingQueue<Boolean> ack = new LinkedBlockingQueue<>();

        @Override
        public long currentTimeMillis() {
            try {
                var t = time.take();
                ack.put(true);
                return t;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        public void awaitConsume(long t) {
            try {
                time.put(t);
                ack.take();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
