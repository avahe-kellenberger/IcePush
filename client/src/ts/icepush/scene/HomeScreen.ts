import {Scene} from "../../engine/game/Scene";
import {ClientAssets} from "../asset/ClientAssets";

export class HomeScreen extends Scene {

    private readonly btnLogin: HTMLButtonElement;

    constructor() {
        super();
        this.btnLogin = document.createElement('button');
        this.btnLogin.innerHTML = 'Login';
        this.btnLogin.className = 'btn-login';
        this.btnLogin.addEventListener('click', () => console.log(`Clicked \'${this.btnLogin.innerText}\'`));
    }

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
        this.getDOMContainer().appendChild(this.btnLogin);
    }

    /**
     * @override
     */
    public onSwitchedFromCurrent(): void {
        // Call getter every time, in case the DOM is modified.
        this.getDOMContainer().removeChild(this.btnLogin);
    }

    /**
     * @override
     */
    public render(ctx: CanvasRenderingContext2D): void {
        // Render the background image.
        ctx.drawImage(ClientAssets.IMAGE_BACKGROUND, 0, 0);
        super.render(ctx);
    }

}