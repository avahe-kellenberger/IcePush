import {Entity} from "./entity/Entity";
import {Game} from "./Game";
import {EventHandler, KeyHandler} from "../input/InputHandler";

/**
 * Represents a Scene being displayed in a game.
 */
export class Scene implements Entity {

    protected readonly game: Game;
    private readonly entities: Set<Entity>;

    private keyHandlers: Set<KeyHandler>|undefined;
    private eventHandlers: Set<EventHandler>|undefined;

    /**
     * Creates a new `Scene` which manages `Entity` objects.
     */
    constructor(game: Game) {
        this.game = game;
        this.entities = new Set();
    }

    // region Input Handlers.

    /**
     * Adds a KeyHandler.
     * This handler is automatically removed when the Game's `Scene` changes, unless `persist` is set to `true`.
     *
     * @param handler The callback invoked when a key event occurs.
     * @param persist If the handler should not be removed automatically when the `Scene` changes.
     * @return If the handler was added.
     */
    public addKeyHandler(handler: KeyHandler, persist: boolean = false): boolean {
        if (!persist) {
            if (this.keyHandlers === undefined) {
                this.keyHandlers = new Set();
            }
            this.keyHandlers.add(handler);
        }
        return this.game.inputHandler.addKeyHandler(handler);
    }

    /**
     * @param handler The handler to remove.
     * @return If the handler was removed.
     */
    public removeKeyHandler(handler: KeyHandler): boolean {
        if (this.keyHandlers !== undefined) {
            this.keyHandlers.delete(handler);
        }
        return this.game.inputHandler.removeKeyHandler(handler);
    }

    /**
     * Adds an EventHandler.
     * This handler is automatically removed when the Game's `Scene` changes, unless `persist` is set to `true`.
     *
     * @param handler The callback invoked when an event occurs.
     * @param persist If the handler should not be removed automatically when the `Scene` changes.
     */
    public addEventHandler(handler: EventHandler, persist: boolean = false): void {
        if (!persist) {
            if (this.eventHandlers === undefined) {
                this.eventHandlers = new Set();
            }
            this.eventHandlers.add(handler);
        }
        this.game.inputHandler.addEventHandler(handler);
    }

    /**
     * @param handler The handler to remove.
     * @return If the handler was removed.
     */
    public removeEventHandler(handler: EventHandler): void {
        if (this.eventHandlers !== undefined) {
            this.eventHandlers.delete(handler);
        }
        this.game.inputHandler.removeEventHandler(handler);
    }

    // endregion

    /**
     * Invoked when the `Game`'s current `Scene` is set as `this` scene.
     */
    public onSwitchedToCurrent(): void {}

    /**
     * Invoked when the `Game`'s current `Scene` is switched from `this` scene to another.
     */
    public onSwitchedFromCurrent(): void {
        if (this.keyHandlers !== undefined) {
            this.keyHandlers.forEach(handler => {
                this.game.inputHandler.removeKeyHandler(handler);
            });
            this.keyHandlers.clear();
        }

        if (this.eventHandlers !== undefined) {
            this.eventHandlers.forEach(handler => {
               this.game.inputHandler.removeEventHandler(handler);
            });
        }
    }

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
