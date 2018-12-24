import {Game} from "../engine/game/Game";
import {HomeScene} from "./scene/HomeScene";
import {GameScene} from "./scene/GameScene";

export class IcePush extends Game {

    private homeScene: HomeScene|undefined;
    private gameScene: GameScene|undefined;

    /**
     *
     */
    constructor(ctx: CanvasRenderingContext2D) {
        super(ctx);
    }

    /**
     * Shows the `HomeScene`.
     * @return If the scene was switched.
     */
    public showHomeScene(): boolean {
        if (this.homeScene === undefined) {
            this.homeScene = new HomeScene(this);
        } else if (this.currentScene === this.homeScene) {
            return false;
        }
        this.setScene(this.homeScene);
        return true;
    }

    /**
     * Shows the `GameScene`.
     * @return If the scene was switched.
     */
    public showGameScene(): boolean {
        if (this.gameScene === undefined) {
            this.gameScene = new GameScene(this);
        } else if (this.currentScene === this.gameScene) {
            return false;
        }
        this.setScene(this.gameScene);
        return true;
    }

    /**
     * @return the DOM element containing the game's canvas.
     */
    public getDOMContainer(): HTMLElement {
        const container: HTMLElement|null = document.getElementById('canvas-container');
        if (container === null) {
            throw new Error('Failed to get DOM element');
        }
        return container;
    }

}