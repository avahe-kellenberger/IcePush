import {Scene} from "../../engine/game/Scene";
import {ClientAssets} from "../asset/ClientAssets";
import {Rectangle} from "../../engine/geom/Rectangle";
import {IcePush} from "../IcePush";

export class GameScene extends Scene {

    // Override the type of `game` in the superclass.
    protected readonly game: IcePush;

    private readonly gameArea: Rectangle;
    private readonly btnLogout: HTMLButtonElement;
    private readonly chatBox: HTMLTextAreaElement;
    private readonly chatInput: HTMLInputElement;

    constructor(game: IcePush) {
        super(game);
        this.game = game;
        /*
         * NOTE: These values were taken directly from the background image,
         * and assumes the canvas fits the same size.
         *
         * It would be wise in the future to have an actual GameArea object that is rendered up a generic background
         * image, which would prevent this poor coding style of hard-coding magic values.
         */
        this.gameArea = new Rectangle(28, 30, 746, 424);

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


        this.chatInput = document.createElement('input');
        this.chatInput.className = 'on-canvas';
        this.chatInput.id = 'chat-input';
        this.chatInput.style.transform = 'translate(-50%, 0%)';
    }

    /**
     * Attempt to log out.
     */
    private logout(): void {
        // TODO: Verify logout event with the server.
        this.game.showHomeScene();
    }

    // region Overridden functions

    /**
     * @override
     */
    public onSwitchedToCurrent(): void {
        // Call getter every time, in case the DOM is modified.
        const container: HTMLElement = this.game.getDOMContainer();
        container.appendChild(this.btnLogout);
        container.appendChild(this.chatBox);
        container.appendChild(this.chatInput);
    }

    /**
     * @override
     */
    public onSwitchedFromCurrent(): void {
        // Call getter every time, in case the DOM is modified.
        const container: HTMLElement = this.game.getDOMContainer();
        container.removeChild(this.btnLogout);
        container.removeChild(this.chatBox);
        container.removeChild(this.chatInput);
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