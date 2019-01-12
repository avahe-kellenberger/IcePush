import {Scene} from "../../engine/game/Scene";
import {ClientAssets} from "../asset/ClientAssets";
import {Rectangle} from "../../engine/math/geom/Rectangle";
import {IcePush} from "../IcePush";
import {Vector2D} from "../../engine/math/Vector2D";
import {InputHandler, KeyHandler} from "../../engine/input/InputHandler";
import {Player} from "../entity/Player";
import {Connection} from "../net/Connection";
import {OPCode} from "../net/OPCode";
import {Entity} from "../../engine/game/entity/Entity";
import {NewPlayerEvent} from "../net/events/NewPlayerEvent";
import {PlayerMoveEvent} from "../net/events/PlayerMoveEvent";
import {PlayerDeathEvent} from "../net/events/PlayerDeathEvent";
import {LogoutEvent} from "../net/events/LogoutEvent";
import {PositionedBuffer} from "../net/PositionedBuffer";
import {ChatReceiveEvent, ChatSendEvent} from "../net/events/ChatEvent";
import {PingEvent} from "../net/events/PingEvent";
import {Time} from "../../engine/time/Time";
import {TimeUpdateEvent} from "../net/events/TimeUpdateEvent";

export class GameScene extends Scene {

    private static readonly PING_TIMEOUT: number = 5.0;

    // region DOM Elements

    private readonly btnLogout: HTMLButtonElement;
    private readonly chatBox: HTMLTextAreaElement;
    private readonly chatInput: HTMLInputElement;
    private readonly domElements: ReadonlySet<HTMLElement>;

    // endregion

    private readonly username: string;
    private readonly gameArea: Rectangle;
    private readonly connection: Connection;

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

        const connection: Connection|undefined = this.getGame().getConnection();
        if (connection === undefined) {
            throw new Error(`The Game's connection should not be null.`);
        }
        this.connection = connection;
        this.connection.addDataReceivedListener(this.dataListener.bind(this));
    }

    /**
     * @param buffer
     */
    private dataListener(buffer: PositionedBuffer): void {
        while (this.isBufferValid(buffer)) {
            this.readEvent(buffer, buffer.readInt8());
        }
    }

    /**
     * Checks if the buffer is in a valid read state.
     * @param buffer The buffer to check.
     */
    private isBufferValid(buffer: PositionedBuffer): boolean {
        const bufferLength: number = buffer.getLength();
        if (buffer.getPosition() >= bufferLength - 1) {
            return false;
        }
        const packetSize: number = buffer.readInt16BE();
        if (packetSize < 0) {
            return false;
        }
        const packetEnd: number = buffer.getPosition() + packetSize - 2;
        return packetEnd <= buffer.getLength();
    }

    /**
     * Handles events received from the server.
     * @param buffer The buffer which contains event data.
     * @param opcode The event's OPCode.
     * @return The size of the event in bytes.
     */
    private readEvent(buffer: PositionedBuffer, opcode: OPCode): void {
        switch (opcode) {
            case OPCode.PING: {
                this.connection.send(new PingEvent());
                break;
            }
            case OPCode.NEW_PLAYER: {
                const event = new NewPlayerEvent(buffer);
                const player: Player = new Player(event.username, event.type);
                player.setDeathCount(event.deathCount);
                player.setIsDead(true);
                this.addEntity(event.playerID, player);
                break;
            }

            case OPCode.PLAYER_MOVE: {
                const event = new PlayerMoveEvent(buffer);
                const player: Entity|undefined = this.getEntity(event.playerID);
                if (player instanceof Player) {
                    player.setLocation(event.location.addVector(this.gameArea.getTopLeft()));
                }
                break;
            }

            case OPCode.PLAYER_DEATH: {
                const event = new PlayerDeathEvent(buffer);
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
                const event = new LogoutEvent(buffer);
                this.removeEntity(event.playerID);
                break;
            }

            case OPCode.CHAT_RECEIVE: {
                const event = new ChatReceiveEvent(buffer);
                this.onMessageReceived(event.chatMessage);
                break;
            }

            case OPCode.UPDATE_TIME: {
                const event = new TimeUpdateEvent(buffer);
                // TODO: Update game time.
                break;
            }
        }
    }

    /**
     * Sends a message to the server.
     * @param message The message to send.
     */
    private sendMessage(message: string): void {
        // TODO: Send a message to a server.
        const chatSendEvent: ChatReceiveEvent = new ChatSendEvent(message);
        const connection: Connection|undefined = this.getGame().getConnection();
        if (connection !== undefined) {
            connection.send(chatSendEvent);
        }
    }

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
     * Adds scene-specific input handlers.
     */
    private addInputHandlers(): void {
        this.addKeyHandler(this.chatKeyHandler());
    }

    /**
     * Handles input for the chat box.
     */
    private chatKeyHandler(): KeyHandler {
        return new KeyHandler(key => {
            if (key.match(/^[a-zA-Z0-9,!?._ +=@#$%^&*()`~\-]$/g) !== null) {
                this.chatInput.value += key;
            } else if (this.chatInput.value.length > 0) {
                if (key === 'Backspace') {
                    this.chatInput.value = this.chatInput.value.slice(0, -1);
                } else if (key === 'Enter') {
                    this.sendMessage(this.chatInput.value);
                    this.chatInput.value = '';
                }
            }
        }, (key, isDown) => isDown);
    }

    /**
     * Calculates the player's current angle of movement based on key input.
     * Angles are 0-255 starting from the bottom, rotating counter-clockwise.
     *
     * @return The "angle" the player is attempting to move in.
     */
    private getMovementAngle(): number|undefined {
        // Arrows keys `keyCodes`
        const LEFT: number = 37;
        const UP: number = 38;
        const RIGHT: number = 39;
        const DOWN: number = 40;

        // Map `key` to `keyCode`
        const keyMap: ReadonlyMap<string, number> = new Map(
            Object.entries({
                'ArrowLeft': LEFT,
                'ArrowUp': UP,
                'ArrowRight': RIGHT,
                'ArrowDown': DOWN
            })
        );

        const inputHandler: InputHandler = this.getGame().inputHandler;

        /**
         *     3
         * 4       2
         *     1
         *
         *     Add together, divide by 2
         *     Multiply by 64
         *     Subtract 64
         */

        // Angles are 0-255 starting from the bottom, rotating counter-clockwise.

        let vertical: number|undefined = undefined;
        let horizontal: number|undefined = undefined;

        if (inputHandler.isKeyDown('ArrowLeft')) {
            horizontal = 192;
        }

        if (inputHandler.isKeyDown('ArrowRight')) {
            horizontal = 64;
        }

        if (inputHandler.isKeyDown('ArrowUp')) {

        }

        if (inputHandler.isKeyDown('ArrowDown')) {

        }

        return undefined;
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
        this.addInputHandlers();
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
            // TODO: Queue events and flush the connection at the end.
            this.connection.send(new PingEvent());
        }

        const currentAngle: number|undefined = this.getMovementAngle();
        if (currentAngle !== this.previousAngle) {

        }
    }

    /**
     * @override
     */
    public render(ctx: CanvasRenderingContext2D): void {
        // Render the background image.
        ctx.drawImage(ClientAssets.IMAGE_BACKGROUND, 0, 0);
        super.render(ctx);
    }

    // endregion

}
