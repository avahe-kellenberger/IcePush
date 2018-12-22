import {Game} from "./Game";
import {Time} from "../time/Time";

export class GameEngine {

    private game: Game;
    private stopped: boolean;

    /**
     * Updates and renders the game.
     * @param game The game to update and render.
     */
    constructor(game: Game) {
        this.game = game;
        this.stopped = true;
    }

    /**
     * Executes game updating and rendering with the given
     * @param elapsed
     */
    private loop(elapsed: number): void {
        // Exit the loop if the engine has been stopped.
        if (this.stopped) {
            return;
        }
        this.game.update(Time.msToSeconds(elapsed));
        this.game.render();
        requestAnimationFrame((time) => this.loop(time));
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
        requestAnimationFrame((time) => this.loop(time));
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