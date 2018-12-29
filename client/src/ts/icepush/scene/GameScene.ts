import {Scene} from "../../engine/game/Scene";
import {ClientAssets} from "../asset/ClientAssets";
import {Rectangle} from "../../engine/math/geom/Rectangle";
import {IcePush} from "../IcePush";
import {Vector2D} from "../../engine/math/Vector2D";
import {KeyHandler} from "../../engine/input/InputHandler";
import {Player} from "../entity/Player";

export class GameScene extends Scene {

    private readonly nick: string;
    private readonly gameArea: Rectangle;

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
        this.nick = nick;
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
        this.chatInput.style.top = '27%';
        this.chatInput.style.left = '50%';
        this.chatInput.style.width = '65%';
        this.chatInput.style.transform = 'translate(-50%, 0%)';
        this.chatInput.disabled = true;
        this.chatInput.readOnly = true;

        // Ensure all elements are in this list.
        this.domElements = new Set([this.btnLogout, this.chatBox, this.chatInput]);
        // endregion

        // TODO: Testing; remove.
        const center: Vector2D = new Vector2D(this.gameArea.getCenterX(), this.gameArea.getCenterY());
        const player: Player = new Player(this.nick, center, ClientAssets.IMAGE_SNOWMAN);
        this.add(player);
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
