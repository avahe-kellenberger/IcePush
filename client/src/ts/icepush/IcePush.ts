import {Game} from "../engine/game/Game";
import {HomeScene} from "./scene/HomeScene";
import {GameScene} from "./scene/GameScene";
import {Connection} from "./net/Connection";
import {LoginEvent} from "./net/events/LoginEvent";

export class IcePush extends Game {

    public static CLIENT_VERSION: number = 105;
    public static SERVER_ADDRESS: string = 'ws://98.11.245.205:2345';
    private static LOGIN_TIMEOUT: number = 3000;

    private connection: Connection|undefined;
    private homeScene: HomeScene|undefined;

    /**
     *
     */
    constructor(ctx: CanvasRenderingContext2D) {
        super(ctx);
    }

    /**
     * @param username
     */
    public tryLogin(username: string): void {
        // Already logged in.
        if (this.connection !== undefined && this.connection.isConnected()) {
            this.onLoginSucceeded(username);
            return;
        }

        this.connection = new Connection(IcePush.SERVER_ADDRESS);
        const connection: Connection = this.connection;

        // Send a login event when the connect first opens.
        this.connection.addOnOpenedListener(() =>
            connection.send(new LoginEvent(IcePush.CLIENT_VERSION, username)));

        // Wait for the connection to time out.
        const timeoutID: NodeJS.Timeout = setTimeout(() => {
                this.onLoginFailed('Connection timed out.');
            }, IcePush.LOGIN_TIMEOUT
        );

        // Await for a message indicating the login succeeded.
        const loginListener = (() => {
            clearTimeout(timeoutID);
            connection.removeMessageListener(loginListener);
            this.onLoginSucceeded(username);
        });
        this.connection.addMessageListener(loginListener);

        // If an error is thrown, the login failed.
        const errorListener = (() => {
            connection.removeErrorListener(errorListener);
            this.onLoginFailed();
        });
        this.connection.addErrorListener(errorListener);

        // If the connection closes, cancel and queued timeouts.
        const closeListener = (() => {
            connection.removeCloseListener(closeListener);
            clearTimeout(timeoutID);
        });
        this.connection.addCloseListener(closeListener);
    }

    /**
     *
     * @param username
     */
    private onLoginSucceeded(username: string): void {
        this.showGameScene(username);
    }

    /**
     *
     * @param reason
     */
    private onLoginFailed(reason?: string): void {
        // TODO: Set status
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
     * @param username The local player's nick name.
     */
    public showGameScene(username: string): void {
        if (!(this.currentScene instanceof GameScene)) {
            this.setScene(new GameScene(this, username));
        }
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