import {Game} from "../engine/game/Game";
import {HomeScene} from "./scene/HomeScene";
import {GameScene} from "./scene/GameScene";
import {Connection} from "./net/Connection";

export class IcePush extends Game {

    public static CLIENT_VERSION: number = 105;
    public static SERVER_ADDRESS: string = 'ws://98.11.245.205:2345';
    private static LOGIN_TIMEOUT: number = 3000;

    private connection: Connection|undefined;
    private homeScene: HomeScene|undefined;
    private username: string|undefined;

    constructor(ctx: CanvasRenderingContext2D) {
        super(ctx);
    }

    /**
     * Attempts to log in.
     * @param username The username to log in with.
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
        // NOTE: The LoginEvent can't be used yet because it is the only event which is not prefixed by its size.
        this.connection.addOnOpenedListener(() =>
            connection.sendLogin(IcePush.CLIENT_VERSION, username));

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
     * @param username The name of the user which logged in.
     */
    private onLoginSucceeded(username: string): void {
        this.username = username;
        this.showGameScene();
    }

    /**
     * @param reason
     */
    private onLoginFailed(reason?: string): void {
        // TODO: Set status
        alert(`Failed to log in!/n${reason}`);
    }

    /**
     * @return The game's connection to the server.
     */
    public getConnection(): Connection|undefined {
        return this.connection;
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
     * @return If switching to the game scene was successful.
     */
    public showGameScene(): boolean {
        if (this.username === undefined || this.currentScene instanceof GameScene) {
            return false;
        }
        this.setScene(new GameScene(this, this.username));
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