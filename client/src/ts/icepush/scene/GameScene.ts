import {Scene} from "../../engine/game/Scene";
import {ClientAssets} from "../asset/ClientAssets";
import {Rectangle} from "../../engine/math/geom/Rectangle";
import {IcePush} from "../IcePush";
import {Vector2D} from "../../engine/math/Vector2D";
import {KeyHandler} from "../../engine/input/InputHandler";
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

export class GameScene extends Scene {

    // region DOM Elements

    private readonly btnLogout: HTMLButtonElement;
    private readonly chatBox: HTMLTextAreaElement;
    private readonly chatInput: HTMLInputElement;
    private readonly domElements: ReadonlySet<HTMLElement>;

    // endregion

    private readonly username: string;
    private readonly gameArea: Rectangle;

    private readonly connection: Connection;

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
     *
     * @param buffer
     */
    private dataListener(buffer: PositionedBuffer): void {
        while (true) {
            const bufferPosition: number = buffer.getPosition();
            const bufferLength: number = buffer.getLength();
            if (bufferPosition + 3 > bufferLength) {
                break;
            }
            // TODO: Unused packet size, needs a server change.
            const packetSize: number = buffer.readInt16BE();
            if (packetSize < 0) {
                break;
            }

            if (bufferPosition + packetSize - 2 > bufferLength) {
                break;
            }

            const opcode: OPCode = buffer.readInt8();
            this.readEvent(buffer, opcode);
        }
    }

    /**
     *
     * @param buffer
     * @param opcode
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
                    // TODO: This location doesn't seem exact.
                    player.setLocation(event.location);
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
        // TODO: Verify logout event with the server.
        this.getGame().showHomeScene();
    }

    /**
     * Adds scene-specific input handlers.
     */
    private addInputHandlers(): void {
        this.addKeyHandler(
            new KeyHandler(key => {
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
            }, (key, isDown) => isDown)
        );

        // TODO: Example KeyHandlers. Network events will need to be dispatched from these keys.
        this.addKeyHandler(new KeyHandler((key, isDown) => console.log(`${key}: ${isDown ? 'keyup' : 'keydown'}`),
            key => key === 'ArrowUp' || key === 'ArrowDown' || key === 'ArrowLeft' || key === 'ArrowRight'
        ));
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
    public render(ctx: CanvasRenderingContext2D): void {
        // Render the background image.
        ctx.drawImage(ClientAssets.IMAGE_BACKGROUND, 0, 0);
        super.render(ctx);
    }

    // endregion

}
