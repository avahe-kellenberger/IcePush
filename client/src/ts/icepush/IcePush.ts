import {Game} from "../engine/game/Game";
import {HomeScene} from "./scene/HomeScene";
import {GameScene} from "./scene/GameScene";

export class IcePush extends Game {

    public static CLIENT_VERSION: number = 105;
    public static SERVER_ADDRESS: string = 'ws://98.11.245.205:2345';

    private homeScene: HomeScene|undefined;

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
     * @param nick The local player's nick name.
     */
    public showGameScene(nick: string): void {
        this.setScene(new GameScene(this, nick));
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