import {Scene} from "../../engine/game/Scene";
import {ClientAssets} from "../asset/ClientAssets";
import {IcePush} from "../IcePush";
import {EventHandler, KeyHandler} from "../../engine/input/InputHandler";

export class HomeScene extends Scene {

    private readonly inputUsername: HTMLInputElement;
    private readonly btnLogin: HTMLButtonElement;
    private readonly btnHelp: HTMLButtonElement;

    constructor(game: IcePush) {
        super(game);
        this.inputUsername = document.createElement("input");
        this.inputUsername.type = 'text';
        this.inputUsername.placeholder = 'Username';
        this.inputUsername.className = 'on-canvas';
        this.inputUsername.style.top = '30%';
        this.inputUsername.style.left = '50%';
        this.inputUsername.style.transform = 'translate(-50%, -50%)';

        this.btnLogin = document.createElement('button');
        this.btnLogin.innerHTML = 'Login';
        this.btnLogin.className = 'on-canvas';
        this.btnLogin.style.top = '40%';
        this.btnLogin.style.left = '50%';
        this.btnLogin.style.transform = 'translate(-112%, -50%)';
        this.btnLogin.addEventListener('click', this.login.bind(this));

        this.btnHelp = document.createElement('button');
        this.btnHelp.innerHTML = 'Help';
        this.btnHelp.className = 'on-canvas';
        this.btnHelp.style.top = '40%';
        this.btnHelp.style.left = '50%';
        this.btnHelp.style.transform = 'translate(12%, -50%)';
        this.btnHelp.addEventListener('click', this.showHelp.bind(this));
    }

    /**
     * Attempt to log in.
     */
    private login(): boolean {
        if (this.inputUsername.value.length < 1 || this.inputUsername.value.match(/^[a-zA-Z0-9]*$/g) === null) {
            alert('Username must be alphanumeric with no spaces.');
            return false;
        }
        // TODO: Validate login with server.
        this.getGame().showGameScene(this.inputUsername.value);
        return true;
    }

    /**
     * Show the help screen.
     */
    private showHelp(): void {
        // TODO: Make a help Scene or help dialog.
        console.log(`Clicked \'${this.btnHelp.innerText}\'`);
    }

    /**
     * Adds scene-specific input handlers.
     */
    private addInputHandlers(): void {
        // Focus the username input field when the canvas is focused or clicked.
        this.addEventHandler(new EventHandler('focus', () => this.inputUsername.focus()));
        this.addEventHandler(new EventHandler('click', () => this.inputUsername.focus()));
        // Login with `Enter` key.
        this.addKeyHandler(new KeyHandler(this.login.bind(this), (key, isDown) => key === 'Enter' && isDown));
    }

    // region Overridden functions

    /**
     * @override
     */
    public onSwitchedToCurrent(): void {
        super.onSwitchedToCurrent();

        // Add input handlers each time the scene is set as the Game's current scene.
        this.addInputHandlers();

        // Call getter every time, in case the DOM is modified.
        const container: HTMLElement = this.getGame().getDOMContainer();
        container.appendChild(this.inputUsername);
        container.appendChild(this.btnLogin);
        container.appendChild(this.btnHelp);
        this.inputUsername.focus();
    }

    /**
     * @override
     */
    public onSwitchedFromCurrent(): void {
        super.onSwitchedFromCurrent();
        // Call getter every time, in case the DOM is modified.
        const container: HTMLElement = this.getGame().getDOMContainer();
        container.removeChild(this.inputUsername);
        container.removeChild(this.btnLogin);
        container.removeChild(this.btnHelp);
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
        // Render the background image first.
        ctx.drawImage(ClientAssets.IMAGE_BACKGROUND, 0, 0);
        super.render(ctx);
    }

    // endregion

}