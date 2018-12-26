import {Scene} from "../../engine/game/Scene";
import {ClientAssets} from "../asset/ClientAssets";
import {Rectangle} from "../../engine/math/geom/Rectangle";
import {IcePush} from "../IcePush";
import {EventHandler} from "../../engine/event/EventHandler";
import {Vector2D} from "../../engine/math/Vector2D";

export class GameScene extends Scene {

    // Override the type of `game` in the superclass.
    protected readonly game: IcePush;

    private readonly nick: string;
    private readonly gameArea: Rectangle;

    // region Event Listeners

    private readonly keyEventHandler: EventHandler;

    // endregion

    // region DOM Elements

    private readonly btnLogout: HTMLButtonElement;
    private readonly chatBox: HTMLTextAreaElement;
    private readonly chatInput: HTMLInputElement;
    private readonly domElements: ReadonlySet<HTMLElement>;

    // endregion

    /**
     * Constructs the scene in which the game is played.
     * @param game
     * @param nick The local user's nick name.
     */
    constructor(game: IcePush, nick: string) {
        super(game);
        this.game = game;
        this.nick = nick;
        /*
         * NOTE: These values were taken directly from the background image,
         * and assumes the canvas fits the same size.
         *
         * It would be wise in the future to have an actual GameArea object that is rendered up a generic background
         * image, which would prevent this poor coding style of hard-coding magic values.
         */
        this.gameArea = new Rectangle(new Vector2D(28, 30), 746, 424);

        // region Event Handlers
        this.keyEventHandler = {
            type: 'keydown',
            listener: (e: KeyboardEvent) => {
                if (e.key.match(/^[a-zA-Z0-9,!?._ +=@#$%^&*()`~\-]$/g) !== null) {
                    this.chatInput.value += e.key;
                } else if (this.chatInput.value.length > 0) {
                    if (e.key === 'Backspace') {
                        this.chatInput.value = this.chatInput.value.slice(0, -1);
                    } else if (e.key === 'Enter') {
                        this.sendMessage(this.chatInput.value);
                        this.chatInput.value = '';
                    }
                }
            }
        };
        // endregion

        // region DOM Elements
        this.btnLogout = document.createElement('button');
        this.btnLogout.className = 'on-canvas';
        this.btnLogout.innerHTML = 'Logout';
        this.btnLogout.style.top = '0%';
        this.btnLogout.style.left = '100%';
        this.btnLogout.style.transform = 'translate(-100%, 0%)';
        this.btnLogout.addEventListener('click', this.logout.bind(this));

        this.chatBox = document.createElement('textarea');
        this.chatBox.className = 'on-canvas';
        this.chatBox.id = 'chatbox';
        this.chatBox.disabled = true;
        this.chatBox.style.transform = 'translate(-50%, 0%)';
        this.chatBox.style.overflow = 'hidden';

        this.chatInput = document.createElement('input');
        this.chatInput.className = 'on-canvas';
        this.chatInput.id = 'chat-input';
        this.chatInput.style.transform = 'translate(-50%, 0%)';
        this.chatInput.disabled = true;

        // Ensure all elements are in this list.
        this.domElements = new Set([this.btnLogout, this.chatBox, this.chatInput]);
        // endregion
    }

    /**
     * Sends a message to the server.
     * @param message The message to send.
     */
    private sendMessage(message: String): void {
        // TODO: Send a message to a server.
        this.onMessageReceived(this.nick, message);
    }

    /**
     * Handles messages received from the server.
     * @param nick The nick of the sender.
     * @param message The message received.
     */
    private onMessageReceived(nick: string, message: String): void {
        this.chatBox.value += `\r\n<${nick}> ${message}`;
        this.chatBox.scrollTop = this.chatBox.scrollHeight - this.chatBox.clientHeight;
    }

    /**
     * Attempt to log out.
     */
    private logout(): void {
        // TODO: Verify logout event with the server.
        this.game.showHomeScene();
    }

    // region Event Listeners

    /**
     * Attaches scene specific event handlers.
     */
    private attachListeners(): void {
        this.game.addEventHandler(this.keyEventHandler);
    }

    /**
     * Removes scene specific event handlers.
     */
    private removeListeners(): void {
        this.game.removeEventHandler(this.keyEventHandler);
    }

    // endregion

    // region DOM

    /**
     * Adds scene specific DOM elements to the document.
     */
    private attachDOMElements(): void {
        // Call getter every time, in case the DOM is modified.
        const container: HTMLElement = this.game.getDOMContainer();
        this.domElements.forEach(e => container.appendChild(e));
    }

    /**
     * Removes scene specific DOM elements from the document.
     */
    private removeDOMElements(): void {
        // Call getter every time, in case the DOM is modified.
        const container: HTMLElement = this.game.getDOMContainer();
        this.domElements.forEach(e => container.removeChild(e));
    }

    // endregion

    // region Overridden functions

    /**
     * @override
     */
    public onSwitchedToCurrent(): void {
        this.attachDOMElements();
        this.attachListeners();
    }

    /**
     * @override
     */
    public onSwitchedFromCurrent(): void {
        // Remove listeners before removing DOM elements to prevent input errors.
        this.removeListeners();
        this.removeDOMElements();
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