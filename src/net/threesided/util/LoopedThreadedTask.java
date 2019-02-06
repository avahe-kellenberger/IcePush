package net.threesided.util;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Executes a task on a new thread, with periodic looping.
 */
public class LoopedThreadedTask {

    private final Consumer<Double> task;
    private Thread taskThread;
    private final Supplier<Boolean> loopCondition;
    private final long loopRateNanos;

    /**
     * Executes the given task on a new thread.
     * @param task The task to execute.
     * @param loopCondition The condition to be met for the task to continue looping.
     * @param loopFrequency Target frequency in seconds to execute the given task, if looped.
     */
    public LoopedThreadedTask(final Consumer<Double> task,
                              final Supplier<Boolean> loopCondition,
                              final double loopFrequency) {
        this.task = task;
        this.loopCondition = loopCondition;
        this.loopRateNanos = (long) loopFrequency * 1000000000;
    }

    /**
     * Starts the task loop if the thread isn't already running.
     */
    public boolean start() {
        if (this.taskThread.isAlive()) {
            return false;
        }
        this.taskThread = new Thread(this::executeTask);
        this.taskThread.start();
        return true;
    }

    /**
     * Stops the underlying `Thread`.
     */
    public void stop() {
        this.taskThread.notify();
        this.taskThread.interrupt();
    }

    /**
     * @param timeoutMillis The time to wait in milliseconds.
     * @throws InterruptedException If another thread has interrupted this current thread.
     * @see Thread#join
     */
    public void join(final long timeoutMillis) throws InterruptedException {
        this.taskThread.join(timeoutMillis);
    }

    /**
     * Loops the given task so long as the `condition` is met and the thread isn't interrupted.
     */
    private void executeTask() {
        /*
         * UNFINISHED
         * TODO: Fix loop logic since it was restructured.
         */

        long lastExecutionTime = System.nanoTime();
        while (!this.taskThread.isInterrupted() && this.loopCondition.get()) {
            final long currentTime = System.nanoTime();
            final long elapsedTime = currentTime - lastExecutionTime;
            this.task.accept(elapsedTime / 1000000000d);

            try {
                // Calculate precise wait time between each loop.
                final long remainingWaitNanos = this.loopRateNanos - elapsedTime;
                if (remainingWaitNanos > 0) {
                    final long waitMillis = (long) Math.floor(remainingWaitNanos / 1000000d);
                    final long waitNanos = remainingWaitNanos - (waitMillis * 1000000);
                    this.taskThread.wait(waitMillis, (int) waitNanos);
                }

                // Update last execution time stamp.
                lastExecutionTime = currentTime;
            } catch (final InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

}
