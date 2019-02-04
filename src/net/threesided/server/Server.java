package net.threesided.server;

import java.util.ArrayList;
import java.util.Collections;

public class Server {

    private ThreadedTask updateTask;

    /**
     *
     */
    public Server() {

    }

}

class ThreadedTask {

    private final ArrayList<Runnable> tasks;

    /**
     *
     * @param task
     * @param loop
     */
    public ThreadedTask(final Runnable task, final boolean loop) {
        this(task, loop, 0f);
    }

    /**
     *
     * @param task
     * @param loop
     * @param targetLoopRate
     */
    public ThreadedTask(final Runnable task, final boolean loop, final float targetLoopRate) {
        this((ArrayList<Runnable>) Collections.singletonList(task), loop, targetLoopRate);
    }

    /**
     * Executes the task
     * @param tasks The tasks to execute.
     * @param loop
     * @param targetLoopRate
     */
    public ThreadedTask(final ArrayList<Runnable> tasks, final boolean loop, final float targetLoopRate) {
        this.tasks = tasks;
    }

    /**
     *
     */
    public void start() {

    }

    /**
     *
     */
    public void stop() {

    }

}
