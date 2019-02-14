package net.threesided.shared;

public class MathUtils {

    /**
     * Picks a random value between min and max.
     * @param min The minimum number (inclusive).
     * @param max The maximum number (exclusive).
     */
    public static double random(final double min, final double max) {
        return min + Math.random() * (max - min);
    }

}
