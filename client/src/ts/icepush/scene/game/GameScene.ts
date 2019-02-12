import {Scene} from "../../../engine/game/Scene";
import {IcePush} from "../../IcePush";
import {InputHandler, KeyHandler} from "../../../engine/input/InputHandler";
import {Player} from "../../entity/Player";
import {Connection} from "../../net/Connection";
import {Entity} from "../../../engine/game/entity/Entity";
import {NewPlayerEvent} from "../../net/events/NewPlayerEvent";
import {ChatReceiveEvent, ChatSendEvent} from "../../net/events/ChatEvent";
import {PingEvent} from "../../net/events/PingEvent";
import {Time} from "../../../engine/time/Time";
import {RoundStartEvent} from "../../net/events/RoundStartEvent";
import {MoveRequestEvent} from "../../net/events/MoveRequestEvent";
import {EndMoveEvent} from "../../net/events/EndMoveEvent";
import {OPCode} from "../../net/NetworkEventBuffer";
import {NetworkEvent} from "../../net/NetworkEvent";
import {PlayerLoggedOutEvent} from "../../net/events/PlayerLoggedOutEvent";
import {PlayerMovedEvent} from "../../net/events/PlayerMovedEvent";
import {PlayerLivesChangedEvent} from "../../net/events/PlayerLivedChangedEvent";
import {RoundWinnersEvent} from "../../net/events/RoundWinnersEvent";
import {RoundStartCountdownEvent} from "../../net/events/RoundStartCountdownEvent";
import {GameplayLayer} from "./GameplayLayer";
import {DOMLayer} from "./DOMLayer";
import {InfoLayer, InfoPane} from "./InfoLayer";
import {Vector2D} from "../../../engine/math/Vector2D";

export class GameScene extends Scene {

    private static readonly PING_TIMEOUT: number = 5.0;

    private readonly playerID: number;
    private readonly connection: Connection;

    private readonly gameplayLayer: GameplayLayer;
    private readonly infoLayer: InfoLayer;
    private readonly domLayer: DOMLayer;

    private previousAngle: number|undefined;

    private roundSecondsRemaining: number|undefined;

    /**
     * Constructs the scene in which the game is played.
     * @param game The scene's parent game.
     * @param playerID The local player's ID number.
     */
    constructor(game: IcePush, playerID: number) {
        super(game);
        this.playerID = playerID;

        this.gameplayLayer = new GameplayLayer();
        this.addLayer(this.gameplayLayer);

        this.domLayer = new DOMLayer(3);
        this.addLayer(this.domLayer);

        const timeRenderLocation: Vector2D = new Vector2D(game.ctx.canvas.width * 0.5,
                                                          game.ctx.canvas.height * 0.3);
        this.infoLayer = new InfoLayer(timeRenderLocation, 2);
        this.addLayer(this.infoLayer);

        // Log out if button is clicked.
        this.domLayer.addLogoutClickListener(() => this.getGame().logout());

        // Process connection last.
        const connection: Connection|undefined = this.getGame().getConnection();
        if (connection === undefined) {
            throw new Error(`The Game's connection should not be null.`);
        }
        this.connection = connection;
        this.connection.addDataReceivedListener(buffer => buffer.getEvents().forEach(this.handleNetworkEvent.bind(this)));

    }

    /**
     * Handles events received from the server.
     * @param e The event to handle.
     */
    private handleNetworkEvent(e: NetworkEvent): void {
        switch (e.getOPCode()) {
            case OPCode.PING: {
                this.connection.enqueueEvent(new PingEvent());
                break;
            }

            case OPCode.NEW_PLAYER: {
                const event: NewPlayerEvent = e as NewPlayerEvent;
                this.onNewPlayerEvent(event);
                break;
            }

            case OPCode.PLAYER_MOVED: {
                const event: PlayerMovedEvent = e as PlayerMovedEvent;
                this.onPlayerMoveEvent(event);
                break;
            }

            case OPCode.PLAYER_LIVES_CHANGED: {
                const event: PlayerLivesChangedEvent = e as PlayerLivesChangedEvent;
                this.onPlayerLivesChangedEvent(event);
                break;
            }

            case OPCode.PLAYER_LOGGED_OUT: {
                const event: PlayerLoggedOutEvent = e as PlayerLoggedOutEvent;
                this.onPlayerLoggedOutEvent(event);
                break;
            }

            case OPCode.CHAT_RECEIVE: {
                const chatMessage: string = (e as ChatReceiveEvent).chatMessage;
                this.domLayer.addChatMessage(chatMessage);
                break;
            }

            case OPCode.ROUND_START: {
                const event: RoundStartEvent = (e as RoundStartEvent);
                this.onRoundStartEvent(event);
                break;
            }

            case OPCode.ROUND_WINNERS: {
                const event: RoundWinnersEvent = e as RoundWinnersEvent;

                const winnerNames: string[] = [];
                for (const id of event.winnerIDs) {
                    const entity: Entity|undefined = this.gameplayLayer.getObject(id);
                    if (entity instanceof Player) {
                        winnerNames.push(entity.getName());
                    }
                }

                // TODO: Display winners in a nice format on the screen.
                break;
            }

            case OPCode.ROUND_START_COUNTDOWN: {
                this.roundSecondsRemaining = (e as RoundStartCountdownEvent).timeMilliseconds / 1000;
                this.infoLayer.setIsRoundCountingDown(true);
                break;
            }

        }
    }

    // region Player Event Receivers

    /**
     * Invoked when a `NewPlayerEvent` is received.
     * @param event The received event.
     */
    private onNewPlayerEvent(event: NewPlayerEvent): void {
        const player: Player = new Player(event.playerID, event.username, event.type, event.lives);
        this.gameplayLayer.setObject(event.playerID, player);

        const isLocalPlayer: boolean = event.playerID === this.playerID;
        const fontColor: string = isLocalPlayer ? InfoPane.LOCAL_PLAYER_FONT_COLOR : InfoPane.DEFAULT_FONT_COLOR;
        const playerInfoPane: InfoPane = new InfoPane(`${player.getName()}\nLives:${player.getLives()}`, fontColor);
        this.infoLayer.setObject(event.playerID, playerInfoPane);
    }

    /**
     * Invoked when a `PlayerLoggedOutEvent` is received.
     * @param event The received event.
     */
    private onPlayerLoggedOutEvent(event: PlayerLoggedOutEvent): void {
        this.gameplayLayer.removeObject(event.playerID);
    }

    /**
     * Invoked when a `PlayerMovedEvent` is received.
     * @param event The received event.
     */
    private onPlayerMoveEvent(event: PlayerMovedEvent): void {
        const player: Entity|undefined = this.gameplayLayer.getObject(event.playerID);
        if (player instanceof Player) {
            const icePlatformTopLeft: Vector2D = this.gameplayLayer.getIcePlatformBounds().getTopLeft();
            const playerLoc: Vector2D = event.location.addVector(icePlatformTopLeft);
            player.setLocation(playerLoc);

            const infoPane: InfoPane = this.infoLayer.getObject(event.playerID) as InfoPane;
            const infoPaneLocation: Vector2D = playerLoc.subtract(0,  player.getSprite().height);
            infoPane.setLocation(infoPaneLocation);
        }
    }

    /**
     * Invoked when a `PlayerLivesChangedEvent` is received.
     * @param event The received event.
     */
    private onPlayerLivesChangedEvent(event: PlayerLivesChangedEvent): void {
        const player: Player|undefined = this.gameplayLayer.getObject(event.playerID) as Player|undefined;
        if (player !== undefined) {
            player.setLives(event.lives);
            if (player.getLives() === 0) {
                this.gameplayLayer.removeObject(event.playerID);
                this.infoLayer.removeObject(event.playerID);
            }
        }
    }

    /**
     * Invoked when a `RoundStartEvent` is received.
     * @param event The received event.
     */
    private onRoundStartEvent(event: RoundStartEvent): void {
        this.roundSecondsRemaining = event.timeMilliseconds / 1000;
        this.infoLayer.setIsRoundCountingDown(false);
    }

    // endregion

    /**
     * Creates the game's chatbox KeyHandler.
     */
    private createChatKeyHandler(): KeyHandler {
        return new KeyHandler(key => {
            if (key.match(/^[ a-zA-Z0-9,'"!?._+=@#$%^&*()`~/\-]$/g) !== null) {
                this.domLayer.appendToChatInputText(key);
            } else if (this.domLayer.getChatInputText().length > 0) {
                if (key === 'Backspace') {
                    this.domLayer.setChatInputText(this.domLayer.getChatInputText().slice(0, -1));
                } else if (key === 'Enter') {
                    this.connection.enqueueEvent(new ChatSendEvent(this.domLayer.getChatInputText()));
                    this.domLayer.clearChatInputText();
                }
            }
        }, (key, isDown) => isDown,
            // Notify on all events, not just state changes.
            true);
    }

    /**
     * Updates the server with the player's current movement information.
     */
    private sendCurrentMovement(): void {
        const currentAngle: number|undefined = this.getMovementAngle();
        if (currentAngle !== this.previousAngle) {
            if (currentAngle !== undefined) {
                this.connection.enqueueEvent(new MoveRequestEvent(currentAngle));
            } else {
                this.connection.enqueueEvent(new EndMoveEvent());
            }
            this.previousAngle = currentAngle;
        }
    }

    /**
     * Calculates the player's current angle of movement based on key input.
     * Angles are 0-255 starting from the bottom, rotating counter-clockwise.
     *
     * @return The "angle" the player is attempting to move in.
     * This method will return `undefined` if the player is not attempting to move,
     * or two opposing keys are being pressed simultaneously.
     */
    private getMovementAngle(): number|undefined {
        const inputHandler: InputHandler = this.getGame().inputHandler;
        const leftPressed: boolean = inputHandler.isKeyDown('ArrowLeft');
        const rightPressed: boolean = inputHandler.isKeyDown('ArrowRight');
        const upPressed: boolean = inputHandler.isKeyDown('ArrowUp');
        const downPressed: boolean = inputHandler.isKeyDown('ArrowDown');

        let vertical: number|undefined = undefined;
        let horizontal: number|undefined = undefined;
        if (leftPressed && !rightPressed) {
            horizontal = 192;
        } else if (!leftPressed && rightPressed) {
            horizontal = 64;
        }

        if (upPressed && !downPressed) {
            vertical = 128;
        } else if (!upPressed && downPressed) {
            vertical = leftPressed ? 255 : 0;
        }

        if (horizontal === undefined) {
            return vertical;
        } else if (vertical === undefined) {
            return horizontal;
        }

        return Math.round((horizontal + vertical) * 0.5);
    }

    // region DOM

    /**
     * Adds scene specific DOM elements to the document.
     */
    private attachDOMElements(): void {
        // Call getter every time, in case the DOM is modified.
        const container: HTMLElement = IcePush.getDOMContainer();
        this.domLayer.getDOMElements().forEach(e => container.appendChild(e));
    }

    /**
     * Removes scene specific DOM elements from the document.
     */
    private removeDOMElements(): void {
        // Call getter every time, in case the DOM is modified.
        const container: HTMLElement = IcePush.getDOMContainer();
        this.domLayer.getDOMElements().forEach(e => container.removeChild(e));
    }

    // endregion

    // region Overridden functions

    /**
     * @override
     */
    public onSwitchedToCurrent(): void {
        super.onSwitchedToCurrent();
        this.attachDOMElements();

        // Add input handlers each time the scene is set as the Game's current scene.
        this.addKeyHandler(this.createChatKeyHandler());
    }

    /**
     * @override
     */
    public onSwitchedFromCurrent(): void {
        super.onSwitchedFromCurrent();
        this.removeDOMElements();
    }

    /**
     * @override
     */
    public getGame(): IcePush {
        return super.getGame() as IcePush;
    }

    /**
     * @override
     */
    public update(delta: number): void {
        super.update(delta);
        this.updateRoundTimeRemaining(delta);
        this.infoLayer.setRoundTimeRemaining(this.roundSecondsRemaining);

        // Send a ping to the server if a message has not been sent recently.
        if (Time.now() - this.connection.getLastSendTime() >= GameScene.PING_TIMEOUT) {
            this.connection.enqueueEvent(new PingEvent());
        }
        this.sendCurrentMovement();
    }

    /**
     * Updates the round time remaining value.
     * @param delta The time elapsed since the last game update.
     */
    private updateRoundTimeRemaining(delta: number): void {
        if (this.gameplayLayer.someEntity(e => e instanceof Player && (e as Player).getUID() !== this.playerID)) {
            if (this.roundSecondsRemaining !== undefined) {
                this.roundSecondsRemaining = Math.max(0, this.roundSecondsRemaining - delta);
            }
        } else {
            this.roundSecondsRemaining = undefined;
        }
    }

    // endregion

}
