import {Game} from "./Game";
import {Time} from "../time/Time";

export class GameEngine {

    private static readonly FPS: number = 60;
    private static readonly FRAME_DELAY: number = 1000 / GameEngine.FPS;

    private game: Game;
    private stopped: boolean;

    private lastTick: number|undefined;

    /**
     * Updates and renders the game.
     * @param game The game to update and render.
     */
    constructor(game: Game) {
        this.game = game;
        this.stopped = true;
    }

    /**
     * Executes game updating and rendering.
     * @return A NodeJS.Timeout object which can cancel the current loop,
     * or null if the loop was terminated internally.
     */
    private loop(): NodeJS.Timeout|null {
        // Exit the loop if the engine has been stopped.
        if (this.stopped) {
            return null;
        }

        if (this.lastTick === undefined) {
            this.lastTick = Time.nowMilliseconds();
        }

        const now: number = Time.nowMilliseconds();
        const elapsed: number = now - this.lastTick;

        this.game.update(elapsed / 1000);
        this.game.render();

        this.lastTick = now;
        const waitTime: number = Math.max(0, GameEngine.FRAME_DELAY - elapsed);
        return setTimeout(this.loop.bind(this), waitTime);
    }

    /**
     * Starts the engine if paused.
     * @return If the engine was started.
     */
    public start(): boolean {
        if (!this.stopped) {
            return false;
        }
        this.stopped = false;
        this.loop();
        return true;
    }

    /**
     * Stops the engine.
     * @return If the engine was stopped.
     */
    public stop(): boolean {
        if (this.stopped) {
            return false;
        }
        this.stopped = true;
        return true;
    }

}