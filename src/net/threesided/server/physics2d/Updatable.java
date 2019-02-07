package net.threesided.server.physics2d;

public interface Updatable {
    /**
     * Updates the object.
     * @param elapsed The elapsed time in seconds since the last update.
     */
    void update(final double elapsed);
}