import {Updatable} from "./entity/Updatable";
import {GameEngine} from "./GameEngine";
import {Scene} from "./Scene";

export class Game implements Updatable {

    protected readonly ctx: CanvasRenderingContext2D;
    protected gameEngine: GameEngine|undefined;
    protected currentScene: Scene|undefined;

    /**
     * Creates a game which is rendered on the given ctx.
     * @param ctx The 2d context of the game ctx.
     */
    constructor(ctx: CanvasRenderingContext2D) {
        this.ctx = ctx;
    }
    /**
     * @return The `Game`'s current `Scene`.
     */
    public getScene(): Scene|undefined {
        return this.currentScene;
    }

    /**
     * Sets the current screen of the game.
     * Only one screen may be shown at a time.
     *
     * @param scene The screen to display.
     * @return If the screen was changed.
     */
    public setScene(scene: Scene): boolean {
        if (this.currentScene === scene) {
            return false;
        }
        const oldScene: Scene|undefined = this.currentScene;
        this.currentScene = scene;

        // Notify the Scenes AFTER they have been set.
        if (oldScene !== undefined) {
            oldScene.onSwitchedFromCurrent();
        }
        this.currentScene.onSwitchedToCurrent();
        return true;
    }

    /**
     * Starts the game.
     * @return If the game was not running and was successfully started.
     */
    public start(): boolean {
        if (this.currentScene === undefined) {
            throw new Error('Cannot start an undefined Scene.');
        }
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
     * @override
     */
    public update(delta: number): void {
        if (this.currentScene !== undefined) {
            this.currentScene.update(delta);
        }
    }

    /**
     * Renders the current scene.
     */
    public render(): void {
        if (this.currentScene !== undefined) {
            this.currentScene.render(this.ctx);
        }
    }

}