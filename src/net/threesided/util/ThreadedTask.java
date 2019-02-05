package net.threesided.util;

/**
 * Executes a task on a new thread, with the option of periodic looping.
 */
public class ThreadedTask {

    final Runnable task;
    Thread taskThread;

    /**
     * Executes the given task on a new thread.
     * @param task The task to execute.
     */
    public ThreadedTask(final Runnable task) {
        this.task = task;
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
     * Executes the given task.
     */
    void executeTask() {
        this.task.run();
    }

}
