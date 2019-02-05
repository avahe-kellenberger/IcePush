package net.threesided.server;

import net.threesided.server.physics2d.Circle;
import net.threesided.server.physics2d.Physics2D;

import java.util.LinkedHashSet;

/**
 *
 */
public class Game {

    private final LinkedHashSet<Circle> entities;
    private final Physics2D physics;

    /**
     *
     */
    public Game() {
        this.entities = new LinkedHashSet<>();
        this.physics = new Physics2D(this.entities.toArray(new Circle[this.entities.size()]));
    }

    /**
     * Adds the entity to the game.
     * @param entity The entity to add.
     * @return If the entity was added.
     */
    public boolean add(final Circle entity) {
        return this.entities.add(entity);
    }

    /**
     * Checks if the given entity is in the game.
     * @param entity The entity to check.
     * @return If the entity exists in the game.
     */
    public boolean contains(final Circle entity) {
        return this.entities.contains(entity);
    }

    /**
     * Removes an entity from the game.
     * @param entity The entity to check.
     * @return If the entity was removed.
     */
    public boolean remove(final Circle entity) {
        return this.entities.remove(entity);
    }

    /**
     * Updates the game.
     * @param elapsed The time in seconds elapsed since the last update.
     */
    public void update(final double elapsed) {
        this.physics.update(elapsed);
    }

}
