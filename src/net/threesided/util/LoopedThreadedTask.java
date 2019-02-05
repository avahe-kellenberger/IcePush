package net.threesided.util;

import java.util.function.Supplier;

public class LoopedThreadedTask extends ThreadedTask {

    private final long loopRateNanos;
    private final Supplier<Boolean> loopCondition;

    /**
     * Executes the given task on a new thread.
     * This task will loop with no delay.
     *
     * @param task The task to execute.
     * @param loopCondition The condition to be met for the task to continue looping.
     */
    public LoopedThreadedTask(final Runnable task, final Supplier<Boolean> loopCondition) {
        this(task, loopCondition, 0);
    }

    /**
     * Executes the given task on a new thread.
     * @param task The task to execute.
     * @param loopCondition The condition to be met for the task to continue looping.
     * @param loopFrequency Target frequency in seconds to execute the given task, if looped.
     */
    public LoopedThreadedTask(Runnable task, Supplier<Boolean> loopCondition, final double loopFrequency) {
        super(task);
        this.loopCondition = loopCondition;
        this.loopRateNanos = (long) loopFrequency * 1000000000;
    }

    /**
     * Loops the given task so long as the `condition` is met and the thread isn't interrupted.
     */
    void executeTask() {
        long lastExecutionTime = System.nanoTime();
        while (!this.taskThread.isInterrupted() && this.loopCondition.get()) {
            this.task.run();
            final long currentTime = System.nanoTime();
            final long elapsedTime = currentTime - lastExecutionTime;

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
