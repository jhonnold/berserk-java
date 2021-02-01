package me.honnold.bitboard;

public class Random {
    private static int state = 123;

    public static int getRandomInt() {
        int number = state;

        number ^= (number << 13);
        number ^= (number >>> 17);
        number ^= (number << 5);

        state = number;
        return number;
    }

    public static long getRandomLong() {
        long n1 = getRandomInt() & 65535;
        long n2 = getRandomInt() & 65535;
        long n3 = getRandomInt() & 65535;
        long n4 = getRandomInt() & 65535;

        return n1 | (n2 << 16) | (n3 << 32) | (n4 << 48);
    }

    public static long getMagic() {
        return getRandomLong() & getRandomLong() & getRandomLong();
    }
}
