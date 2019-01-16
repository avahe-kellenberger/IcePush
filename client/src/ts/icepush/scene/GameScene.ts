import {Scene} from "../../engine/game/Scene";
import {ClientAssets} from "../asset/ClientAssets";
import {Rectangle} from "../../engine/math/geom/Rectangle";
import {IcePush} from "../IcePush";
import {Vector2D} from "../../engine/math/Vector2D";
import {InputHandler, KeyHandler} from "../../engine/input/InputHandler";
import {Player} from "../entity/Player";
import {Connection} from "../net/Connection";
import {Entity} from "../../engine/game/entity/Entity";
import {NewPlayerEvent} from "../net/events/NewPlayerEvent";
import {PlayerDeathEvent} from "../net/events/PlayerDeathEvent";
import {ChatReceiveEvent, ChatSendEvent} from "../net/events/ChatEvent";
import {PingEvent} from "../net/events/PingEvent";
import {Time} from "../../engine/time/Time";
import {TimeRemainingEvent} from "../net/events/TimeRemainingEvent";
import {MoveRequestEvent} from "../net/events/MoveRequestEvent";
import {EndMoveEvent} from "../net/events/EndMoveEvent";
import {OPCode} from "../net/NetworkEventBuffer";
import {NetworkEvent} from "../net/NetworkEvent";
import {PlayerLoggedOutEvent} from "../net/events/PlayerLoggedOutEvent";
import {PlayerMovedEvent} from "../net/events/PlayerMovedEvent";

export class GameScene extends Scene {

    private static readonly PING_TIMEOUT: number = 5.0;

    private readonly username: string;
    private readonly gameArea: Rectangle;
    private readonly connection: Connection;

    // region DOM Elements

    private readonly btnLogout: HTMLButtonElement;
    private readonly chatBox: HTMLTextAreaElement;
    private readonly chatInput: HTMLInputElement;
    private readonly domElements: ReadonlySet<HTMLElement>;

    // endregion

    private previousAngle: number|undefined;

    /**
     * Constructs the scene in which the game is played.
     * @param game
     * @param username The local player's username.
     */
    constructor(game: IcePush, username: string) {
        super(game);
        this.username = username;
        /*
         * NOTE: These values were taken directly from the background image,
         * and assumes the canvas fits the same size.
         *
         * It would be wise in the future to have an actual GameArea object that is rendered up a generic background
         * image, which would prevent this poor coding style of hard-coding magic values.
         */
        this.gameArea = new Rectangle(new Vector2D(28, 30), 746, 424);

        const connection: Connection|undefined = this.getGame().getConnection();
        if (connection === undefined) {
            throw new Error(`The Game's connection should not be null.`);
        }
        this.connection = connection;
        this.connection.addDataReceivedListener(buffer => buffer.getEvents().forEach(this.handleNetworkEvent.bind(this)));

        // region DOM Elements
        this.btnLogout = document.createElement('button');
        this.btnLogout.className = 'on-canvas unfocusable';
        this.btnLogout.innerHTML = 'Logout';
        this.btnLogout.style.top = '0%';
        this.btnLogout.style.left = '100%';
        this.btnLogout.style.transform = 'translate(-100%, 0%)';
        this.btnLogout.addEventListener('click', this.logout.bind(this));

        this.chatBox = document.createElement('textarea');
        this.chatBox.className = 'on-canvas unfocusable';
        this.chatBox.id = 'chatbox';
        this.chatBox.style.padding = '0';
        this.chatBox.style.margin = '0';
        this.chatBox.style.top = '0';
        this.chatBox.style.left = '50%';
        this.chatBox.style.width = '65%';
        this.chatBox.style.height = '25%';
        this.chatBox.style.transform = 'translate(-50%, 0%)';
        this.chatBox.style.overflow = 'hidden';
        this.chatBox.disabled = true;
        this.chatBox.readOnly = true;

        this.chatInput = document.createElement('input');
        this.chatInput.className = 'on-canvas unfocusable';
        this.chatInput.id = 'chat-input';
        this.chatInput.style.padding = '0';
        this.chatInput.style.margin = '0';
        this.chatInput.style.top = '25%';
        this.chatInput.style.left = '50%';
        this.chatInput.style.width = '65%';
        this.chatInput.style.transform = 'translate(-50%, 0%)';
        this.chatInput.disabled = true;
        this.chatInput.readOnly = true;

        // Ensure all elements are in this list.
        this.domElements = new Set([this.btnLogout, this.chatBox, this.chatInput]);
        // endregion
    }

    // region Networking

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
                const player: Player = new Player(event.username, event.type);
                player.setDeathCount(event.deathCount);
                player.setIsDead(true);
                this.addEntity(event.playerID, player);
                break;
            }

            case OPCode.PLAYER_MOVE: {
                const event: PlayerMovedEvent = e as PlayerMovedEvent;
                const player: Entity|undefined = this.getEntity(event.playerID);
                if (player instanceof Player) {
                    player.setLocation(event.location.addVector(this.gameArea.getTopLeft()));
                }
                break;
            }

            case OPCode.PLAYER_DEATH: {
                const event = e as PlayerDeathEvent;
                const player: Entity|undefined = this.getEntity(event.playerID);
                if (!(player instanceof Player)) {
                    break;
                }
                player.setDeathCount(event.deathCount);
                if (player.getDeathCount() !== 0) {
                    // Death reset; not dead.
                    player.setIsDead(true);
                }
                break;
            }

            case OPCode.PLAYER_LOGGED_OUT: {
                const event = e as PlayerLoggedOutEvent;
                this.removeEntity(event.playerID);
                break;
            }

            case OPCode.CHAT_RECEIVE: {
                const event = e as ChatReceiveEvent;
                this.onMessageReceived(event.chatMessage);
                break;
            }

            case OPCode.UPDATE_TIME: {
                const event = e as TimeRemainingEvent;
                // TODO: Update round time remaining.
                break;
            }
        }
    }

    // endregion

    /**
     * Handles messages received from the server.
     * @param message The message received.
     */
    private onMessageReceived(message: String): void {
        this.chatBox.value += `${message}\n`;
        this.chatBox.scrollTop = this.chatBox.scrollHeight - this.chatBox.clientHeight;
    }

    /**
     * Attempt to log out.
     */
    private logout(): void {
        this.getGame().logout();
    }

    /**
     * Creates the game's chatbox KeyHandler.
     */
    private createChatKeyHandler(): KeyHandler {
        return new KeyHandler(key => {
            if (key.match(/^[ a-zA-Z0-9,'"!?._+=@#$%^&*()`~/\-]$/g) !== null) {
                this.chatInput.value += key;
            } else if (this.chatInput.value.length > 0) {
                if (key === 'Backspace') {
                    this.chatInput.value = this.chatInput.value.slice(0, -1);
                } else if (key === 'Enter') {
                    this.connection.enqueueEvent(new ChatSendEvent(this.chatInput.value));
                    this.chatInput.value = '';
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
        const container: HTMLElement = this.getGame().getDOMContainer();
        this.domElements.forEach(e => container.appendChild(e));
    }

    /**
     * Removes scene specific DOM elements from the document.
     */
    private removeDOMElements(): void {
        // Call getter every time, in case the DOM is modified.
        const container: HTMLElement = this.getGame().getDOMContainer();
        this.domElements.forEach(e => container.removeChild(e));
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

        // Send a ping to the server if a message has not been sent recently.
        if (Time.now() - this.connection.getLastSendTime() >= GameScene.PING_TIMEOUT) {
            this.connection.enqueueEvent(new PingEvent());
        }
        this.sendCurrentMovement();

        // Send all events that have been enqueued this update.
        this.connection.flushEventQueue();
    }

    /**
     * @override
     */
    public render(ctx: CanvasRenderingContext2D): void {
        // Render the background image before the rest of the scene.
        ctx.drawImage(ClientAssets.IMAGE_BACKGROUND, 0, 0);
        super.render(ctx);
    }

    // endregion

}
