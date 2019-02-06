package net.threesided.util;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Executes a task on a new thread, with periodic looping.
 */
public class LoopedThreadedTask {

    private final Consumer<Double> task;
    private final Thread taskThread;
    private final Supplier<Boolean> loopCondition;
    private final long loopFrequencyNanos;

    /**
     * Executes the given task on a new thread.
     * @param task The task to execute.
     * @param loopCondition The condition to be met for the task to continue looping.
     * @param loopFrequency Target frequency in seconds to execute the given task.
     */
    public LoopedThreadedTask(final Consumer<Double> task,
                              final Supplier<Boolean> loopCondition,
                              final double loopFrequency) {
        this.task = task;
        this.loopCondition = loopCondition;
        this.loopFrequencyNanos = (long) (loopFrequency * 1000000000);
        this.taskThread = new Thread(this::loopTask);
    }

    // region Thread Encapsulation

    /**
     * Starts the task loop if the thread isn't already running.
     * The task can only be started once, as it is backed by a Thread.
     */
    public boolean start() {
        if (this.taskThread.isAlive()) {
            return false;
        }
        synchronized (this.taskThread) {
            this.taskThread.start();
        }
        return true;
    }

    /**
     * Stops the underlying `Thread`.
     */
    public void stop() {
        synchronized (this.taskThread) {
            this.taskThread.notify();
            this.taskThread.interrupt();
        }
    }

    /**
     * @param timeoutMillis The time to wait in milliseconds.
     * @throws InterruptedException If another thread has interrupted this current thread.
     * @see Thread#join
     */
    public void join(final long timeoutMillis) throws InterruptedException {
        synchronized (this.taskThread) {
            this.taskThread.join(timeoutMillis);
        }
    }

    // endregion

    /**
     * Loops the given task so long as the loopCondition is met and the thread isn't interrupted.
     */
    private void loopTask() {
        long elapsedTimeInNanos = 0;
        while (!this.taskThread.isInterrupted() && this.loopCondition.get()) {
            final long iterationStartTimeNanos = System.nanoTime();

            // Pass the elapsed time in seconds to the task's callback.
            this.task.accept(this.nanosToSeconds(elapsedTimeInNanos));

            // Calculate the elapsed time in nanoseconds since the loop began.
            elapsedTimeInNanos = System.nanoTime() - iterationStartTimeNanos;

            // Wait until the total nanoseconds quantified by this.loopFrequencyNanos has elapsed.
            final long waitedNanos = this.waitForLoopFrequencyRemainder(elapsedTimeInNanos);

            // Assign the newly total-elapsed time, to be passed to the task's callback.
            elapsedTimeInNanos += waitedNanos;
        }
    }

    /**
     * Invokes LoopedThreadedTask#waitTaskSynchronously for the amount of time to be waited
     * until {@link LoopedThreadedTask#loopFrequencyNanos} has elapsed.
     * @param elapsedNanos The quantity of nanoseconds elapsed so far.
     * @return The quantity of nanosecond waited by this method.
     */
    private long waitForLoopFrequencyRemainder(final long elapsedNanos) {
        final long remainingWaitNanos = this.loopFrequencyNanos - elapsedNanos;
        if (remainingWaitNanos > 0) {
            // Calculate precise wait time between each loop.
            final long waitMillis = this.nanosToMilliseconds(remainingWaitNanos);
            final int waitNanos = (int) (remainingWaitNanos - this.millisecondsToNanos(waitMillis));
            this.waitTaskSynchronously(waitMillis, waitNanos);
        }
        return remainingWaitNanos;
    }

    /**
     * Synchronously calls Object#wait on the taskThread for the given time.
     * @param waitMillis The number of milliseconds to wait.
     * @param waitNanos The number of nanoseconds to wait.
     */
    private void waitTaskSynchronously(final long waitMillis, final int waitNanos) {
        synchronized (this.taskThread) {
            try {
                this.taskThread.wait(waitMillis, waitNanos);
            } catch (final InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Converts nanoseconds to seconds.
     * @param nanoSeconds The number of nanoseconds.
     * @return The seconds decimal representation of the given nanoseconds.
     */
    private double nanosToSeconds(final long nanoSeconds) {
        return nanoSeconds / 1000000000d;
    }

    /**
     * Converts nanoseconds to whole milliseconds (floor operation).
     * @param nanoSeconds The number of nanoseconds to convert.
     * @return The number of whole milliseconds which represent the given time in nanoseconds.
     */
    private long nanosToMilliseconds(final long nanoSeconds) {
        return (long) Math.floor(nanoSeconds / 1000000d);
    }

    /**
     * Converts milliseconds to nanoseconds.
     * @param millis The number of milliseconds to convert.
     * @return The number of nanoseconds which represent the given time in milliseconds.
     */
    private long millisecondsToNanos(final long millis) {
        return millis * 1000000;
    }

}
