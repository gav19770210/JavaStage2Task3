package ru.gav19770210.stage2task3;

public class MainApp {

    public static void main(String[] args) throws InterruptedException {
        var fraction = new Fraction(2, 3);
        System.out.println("fraction.doubleValue: " + fraction.doubleValue());

        var fractionCache = (Fractionable) CacheFactory.makeCacheable(fraction);
        System.out.println("CacheCleaner: " + CacheInvocationHandler.getCacheCleaner());

        System.out.println("fractionCache.doubleValue: " + fractionCache.doubleValue());
        System.out.println("fractionCache.doubleValue: " + fractionCache.doubleValue());

        fractionCache.setNum(5);
        System.out.println("fractionCache.doubleValue: " + fractionCache.doubleValue());
        System.out.println("fractionCache.doubleValue: " + fractionCache.doubleValue());

        fractionCache.setNum(2);
        System.out.println("fractionCache.doubleValue: " + fractionCache.doubleValue());
        System.out.println("fractionCache.doubleValue: " + fractionCache.doubleValue());

        var fraction2 = new Fraction(3, 4);
        Fractionable fractionCache2 = (Fractionable) CacheFactory.makeCacheable(fraction2);

        System.out.println("fractionCache2.doubleValue: " + fractionCache2.doubleValue());
        System.out.println("fractionCache2.doubleValue: " + fractionCache2.doubleValue());

        Thread.sleep(2000);

        System.out.println("fractionCache2.doubleValue: " + fractionCache2.doubleValue());
        System.out.println("fractionCache2.doubleValue: " + fractionCache2.doubleValue());

        System.out.println("fractionCache.doubleValue: " + fractionCache.doubleValue());
        System.out.println("fractionCache.doubleValue: " + fractionCache.doubleValue());

        CacheFactory.clearCacheStorages();
    }

    interface Fractionable {
        void setNum(int num);

        void setDenum(int denum);

        double doubleValue();
    }

    @CacheCleanerConfig(cacheCleanerType = CacheCleanerType.GET_VALUE)
    //@CacheCleanerConfig(cacheCleanerType = CacheCleanerType.BACKGROUND)
    static class Fraction implements Fractionable {
        private int num;
        private int denum;

        public Fraction(int num, int denum) {
            this.num = num;
            this.denum = denum;
        }

        @Override
        @Mutator
        public void setNum(int num) {
            this.num = num;
        }

        @Override
        @Mutator
        public void setDenum(int denum) {
            this.denum = denum;
        }

        @Override
        @Cache(expirePeriod = 1000)
        public double doubleValue() {
            System.out.println("Call Fraction.doubleValue");
            return (double) num / denum;
        }
    }
}
