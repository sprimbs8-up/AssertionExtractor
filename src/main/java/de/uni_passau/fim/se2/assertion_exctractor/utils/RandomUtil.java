package de.uni_passau.fim.se2.assertion_exctractor.utils;

import java.util.Random;

/**
 * The {@link RandomUtil} class is a singleton utility for managing a Random instance. It provides methods to initialize
 * the Random instance with a seed and retrieve it.
 */
public final class RandomUtil {

    // Singleton instance
    private static RandomUtil instance;
    // Random instance
    private Random random;

    private RandomUtil() {
    }

    /**
     * Returns the singleton instance of the RandomUtil class.
     *
     * @return The singleton instance of the RandomUtil class.
     */
    public static RandomUtil getInstance() {
        if (instance == null) {
            instance = new RandomUtil();
        }
        return instance;
    }

    /**
     * Initializes the Random instance with the specified seed.
     *
     * @param seed The seed value to initialize the Random instance.
     */
    public void initializeRandom(int seed) {
        random = new Random(seed);
    }

    /**
     * Retrieves the Random instance.
     *
     * @return The Random instance.
     */
    public Random getRandom() {
        return random;
    }
}
