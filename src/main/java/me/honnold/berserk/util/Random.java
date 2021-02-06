package me.honnold.berserk.util;

public class Random {
    private static final Random singleton = new Random();
    private int state = (int) System.currentTimeMillis();

    private Random() {}

    public static Random getInstance() {
        return singleton;
    }

    public int getRandomInt() {
        int number = state;

        number ^= (number << 13);
        number ^= (number >>> 17);
        number ^= (number << 5);

        state = number;
        return number;
    }

    public long getRandomLong() {
        long n1 = getRandomInt() & 65535;
        long n2 = getRandomInt() & 65535;
        long n3 = getRandomInt() & 65535;
        long n4 = getRandomInt() & 65535;

        return n1 | (n2 << 16) | (n3 << 32) | (n4 << 48);
    }

    public long getMagic() {
        return getRandomLong() & getRandomLong() & getRandomLong();
    }
}
