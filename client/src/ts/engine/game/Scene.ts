import {Entity} from "./entity/Entity";
import {Game} from "./Game";

/**
 * Represents a Scene being displayed in a game.
 */
export class Scene implements Entity {

    protected readonly game: Game;
    private readonly entities: Set<Entity>;

    /**
     * Creates a new `Scene` which manages `Entity` objects.
     */
    constructor(game: Game) {
        this.game = game;
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
     * Checks if the scene contains the entity.
     * @param entity The entity to check.
     * @return If the scene contains the entity.
     */
    public contains(entity: Entity): boolean {
        return this.entities.has(entity);
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
     * Removes all entities from the scene.
     */
    public removeAll(): void {
        this.entities.clear();
    }

    /**
     * Invokes a callback on each `Entity` in the scene.
     * @param callback The callback to invoke.
     */
    public forEach(callback: (e?: Entity) => void): void {
        this.entities.forEach(callback);
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
