import {Updatable} from "./entity/Updatable";
import {GameEngine} from "./GameEngine";
import {Scene} from "./Scene";
import {InputHandler, KeyListener} from "../input/InputHandler";
import {EventHandler} from "../input/EventHandler";

export class Game implements Updatable {

    public readonly ctx: CanvasRenderingContext2D;
    protected gameEngine: GameEngine|undefined;
    protected currentScene: Scene|undefined;
    private inputHandler: InputHandler|undefined;

    /**
     * Creates a game which is rendered on the given ctx.
     * @param ctx The 2d context of the game ctx.
     */
    constructor(ctx: CanvasRenderingContext2D) {
        this.ctx = ctx;
        this.ctx.canvas.tabIndex = 0;
    }

    // region Scene Handling

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

    // endregion

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
        return this.gameEngine !== undefined ? this.gameEngine.stop() : false;
    }

    // region Event Handlers

    /**
     * Adds an `EventHandler` to the DOM.
     * @param handler The event handler.
     */
    public addEventHandler(handler: EventHandler): void {
        document.addEventListener(handler.type, handler.listener);
    }

    /**
     * Removes an `EventHandler` from the DOM.
     * @param handler The event handler.
     */
    public removeEventHandler(handler: EventHandler): void {
        document.removeEventListener(handler.type, handler.listener);
    }


    // region InputHandler

    /**
     * Adds a KeyListener.
     * @param key The key of which to listen for state changes.
     * @param listener The callback invoked when the state of the key changes.
     * @return If the listener was added.
     */
    public addKeyListener(key: string, listener: KeyListener): boolean {
        if (this.inputHandler === undefined) {
            this.inputHandler = new InputHandler(document);
        }
        return this.inputHandler.addKeyListener(key, listener);
    }

    /**
     * Removes a KeyListener.
     * @param key The key of which to listen for state changes.
     * @param listener The callback invoked when the state of the key changes.
     * @return If the listener was removed.
     */
    public removeKeyListener(key: string, listener: KeyListener): boolean {
        return this.inputHandler === undefined || this.inputHandler.removeKeyListener(key, listener);
    }

    // endregion

    // endregion

    // region Overridden functions

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
            this.ctx.clearRect(0, 0, this.ctx.canvas.width, this.ctx.canvas.height);
            this.currentScene.render(this.ctx);
        }
    }

    // endregion

}