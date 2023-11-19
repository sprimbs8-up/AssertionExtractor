package de.uni_passau.fim.se2.assertion_exctractor.utils;

import java.util.Random;

public final class RandomUtil {

    private static RandomUtil instance;
    private Random random;

    private RandomUtil() {
    }

    public static RandomUtil getInstance() {
        if (instance == null) {
            instance = new RandomUtil();
        }
        return instance;
    }

    public void initializeRandom(int seed) {
        random = new Random(seed);
    }

    public Random getRandom() {
        return random;
    }
}
