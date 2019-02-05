package net.threesided.util;

import java.util.function.Supplier;

/**
 * Executes a task on a new thread, with the option of periodic looping.
 */
public class ThreadedTask {

    private final Supplier<Boolean> condition;
    private final Runnable task;
    private final boolean loop;
    private final long targetLoopRate;

    private Thread taskThread;

    /**
     * Executes the given task on a new thread.
     * @param task The task to execute.
     */
    public ThreadedTask(final Runnable task, final Supplier<Boolean> condition) {
        this(task, condition, false, 0);
    }

    /**
     * Executes the given task on a new thread.
     * @param task The task to execute.
     * @param loop If the task should loop.
     * @param loopRateMillis Target frequency in milliseconds to execute the given task, if looped.
     */
    public ThreadedTask(final Runnable task, final Supplier<Boolean> condition, final boolean loop, final long loopRateMillis) {
        this.task = task;
        this.condition = condition;
        this.loop = loop;
        this.targetLoopRate = loopRateMillis;
    }

    /**
     * Loops the given task so long as the `condition` is met and the thread isn't interrupted.
     */
    private void loopTask() {
        long lastExecutionTimeMillis = System.currentTimeMillis();
        while (!this.taskThread.isInterrupted() && this.condition.get()) {
            this.task.run();
            final long currentTimeMillis = System.currentTimeMillis();
            final long elapsedMillis = currentTimeMillis - lastExecutionTimeMillis;

            try {
                this.taskThread.wait(Math.max(0, this.targetLoopRate - elapsedMillis));
                lastExecutionTimeMillis = currentTimeMillis;
            } catch (final InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Starts the task loop if the thread isn't already running.
     */
    public boolean start() {
        if (this.taskThread.isAlive()) {
            return false;
        }
        this.taskThread = new Thread(this.loop ? this::loopTask : this.task);
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

}
