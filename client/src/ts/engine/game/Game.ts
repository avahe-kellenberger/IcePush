import {Updatable} from "./entity/Updatable";
import {GameEngine} from "./GameEngine";
import {Scene} from "./Scene";

export class Game implements Updatable {

    protected readonly ctx: CanvasRenderingContext2D;
    protected gameEngine: GameEngine|undefined;
    protected currentScene: Scene;

    /**
     * Creates a game which is rendered on the given ctx.
     * @param scene
     * @param ctx The 2d context of the game ctx.
     */
    protected constructor(scene: Scene, ctx: CanvasRenderingContext2D) {
        this.currentScene = scene;
        this.ctx = ctx;
    }

    /**
     * @return The `Game`'s current `Scene`.
     */
    public getScene(): Scene {
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
        this.currentScene = scene;
        return true;
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
     * @override
     */
    public update(delta: number): void {
        this.currentScene.update(delta);
    }

    /**
     * Renders the current scene.
     */
    public render(): void {
        this.currentScene.render(this.ctx);
    }

}