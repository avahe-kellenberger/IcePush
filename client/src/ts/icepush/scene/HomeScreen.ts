import {Scene} from "../../engine/game/Scene";
import {ClientAssets} from "../asset/ClientAssets";

export class HomeScreen extends Scene {

    private readonly inputUsername: HTMLInputElement;
    private readonly btnLogin: HTMLButtonElement;
    private readonly btnHelp: HTMLButtonElement;

    constructor() {
        super();
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
        console.log(`Attempting login as \'${this.inputUsername.value}\'`);
        return false;
    }

    /**
     * Show the help screen.
     */
    private showHelp(): void {
        console.log(`Clicked \'${this.btnHelp.innerText}\'`);
    }

    // region Overridden functions

    /**
     * Retrieves the container element on the DOM.
     */
    private getDOMContainer(): HTMLElement {
        const container: HTMLElement|null = document.getElementById('container');
        if (container === null) {
            throw new Error('Failed to get DOM element');
        }
        return container;
    }

    /**
     * @override
     */
    public onSwitchedToCurrent(): void {
        // Call getter every time, in case the DOM is modified.
        const container: HTMLElement = this.getDOMContainer();
        container.appendChild(this.inputUsername);
        container.appendChild(this.btnLogin);
        container.appendChild(this.btnHelp);
    }

    /**
     * @override
     */
    public onSwitchedFromCurrent(): void {
        // Call getter every time, in case the DOM is modified.
        const container: HTMLElement = this.getDOMContainer();
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