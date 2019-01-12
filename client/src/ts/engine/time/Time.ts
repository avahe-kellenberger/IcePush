const MILLISECONDS_TO_SECONDS = 1.0 / 1000;

export class Time {

    /**
     * @return The current time in seconds.
     */
    public static now(): number {
        return Time.msToSeconds(Time.nowMilliseconds());
    }

    /**
     * @return The current time in milliseconds.
     */
    public static nowMilliseconds(): number {
        return performance.now();
    }

    /**
     * Converts milliseconds to seconds.
     * @param milliseconds The milliseconds to convert.
     */
    public static msToSeconds(milliseconds: number): number {
        return milliseconds * MILLISECONDS_TO_SECONDS;
    }

}