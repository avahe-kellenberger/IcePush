import {Updatable} from "./Updatable";
import {GameEngine} from "./GameEngine";
import {Entity} from "./Entity";

export class Game implements Updatable {

    protected readonly ctx: CanvasRenderingContext2D;
    protected gameEngine: GameEngine|undefined;
    protected readonly entities: Set<Entity>;

    /**
     * Creates a game which is rendered on the given ctx.
     * @param ctx The 2d context of the game ctx.
     */
    protected constructor(ctx: CanvasRenderingContext2D) {
        this.ctx = ctx;
        this.entities = new Set();
    }

    /**
     * Starts the game.
     * @return If the game was not running and was successfully started.
     */
    public start(): boolean {
        if (this.gameEngine === undefined) {
            this.gameEngine = new GameEngine(this);
        }
        return this.gameEngine.start();
    }

    /**
     * Pauses the game.
     * @return If the game was running and was successfully paused.
     */
    public pause(): boolean {
        if (this.gameEngine !== undefined) {
            return this.gameEngine.stop();
        }
        return false;
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
     * Renders the contents of the game.
     */
    public render(): void {
        this.entities.forEach(e => e.render(this.ctx));
    }

}