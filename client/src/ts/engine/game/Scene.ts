import {Entity} from "./entity/Entity";

/**
 * Represents a Scene being displayed in a game.
 */
export class Scene implements Entity {

    private readonly entities: Set<Entity>;

    /**
     * Creates a new `Scene` which manages `Entity` objects.
     */
    constructor() {
        this.entities = new Set();
    }

    /**
     * Invoked when the `Game`'s current `Scene` is set as `this` scene.
     */
    public onSwitchedToCurrent(): void {}

    /**
     * Invoked when the `Game`'s current `Scene` is switched from `this` scene to another.
     */
    public onSwitchedFromCurrent(): void {}

    /**
     * Adds an `Entity` to the game.
     * @param entity The Entity to add.
     * @return If the entity was added successfully.
     */
    public add(entity: Entity): boolean {
        return this.entities.size !== this.entities.add(entity).size;
    }

    /**
     * Removes an `Entity` from the game.
     * @param entity The Entity to remove.
     * @return If the entity was removed successfully.
     */
    public remove(entity: Entity): boolean {
        return this.entities.delete(entity);
    }

    /**
     * @override
     */
    public update(delta: number): void {
        this.entities.forEach(e => e.update(delta));
    }

    /**
     * @override
     */
    public render(ctx: CanvasRenderingContext2D): void {
        this.entities.forEach(e => e.render(ctx));
    }

}
