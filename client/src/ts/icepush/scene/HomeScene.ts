import {Scene} from "../../engine/game/Scene";
import {ClientAssets} from "../asset/ClientAssets";
import {IcePush} from "../IcePush";

export class HomeScene extends Scene {

    // Override the type of `game` in the superclass.
    protected readonly game: IcePush;

    private readonly inputUsername: HTMLInputElement;
    private readonly btnLogin: HTMLButtonElement;
    private readonly btnHelp: HTMLButtonElement;

    constructor(game: IcePush) {
        super(game);
        this.game = game;
        this.inputUsername = document.createElement("input");
        this.inputUsername.type = 'text';
        this.inputUsername.placeholder = 'Username';
        this.inputUsername.className = 'on-canvas';
        this.inputUsername.style.top = '30%';
        this.inputUsername.style.left = '50%';
        this.inputUsername.style.transform = 'translate(-50%, -50%)';
        this.inputUsername.addEventListener('keydown', (e) => {
           if (e.key === 'Enter') {
               this.login();
           }
        });

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
        this.game.showGameScene(this.inputUsername.value);
        return true;
    }

    /**
     * Show the help screen.
     */
    private showHelp(): void {
        // TODO: Make a help Scene or help dialog.
        console.log(`Clicked \'${this.btnHelp.innerText}\'`);
    }

    // region Overridden functions

    /**
     * @override
     */
    public onSwitchedToCurrent(): void {
        // Call getter every time, in case the DOM is modified.
        const container: HTMLElement = this.game.getDOMContainer();
        container.appendChild(this.inputUsername);
        container.appendChild(this.btnLogin);
        container.appendChild(this.btnHelp);
        this.inputUsername.focus();
    }

    /**
     * @override
     */
    public onSwitchedFromCurrent(): void {
        // Call getter every time, in case the DOM is modified.
        const container: HTMLElement = this.game.getDOMContainer();
        container.removeChild(this.inputUsername);
        container.removeChild(this.btnLogin);
        container.removeChild(this.btnHelp);
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