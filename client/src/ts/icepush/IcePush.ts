import {Game} from "../engine/game/Game";
import {ClientAssets} from "./asset/ClientAssets";

export class IcePush extends Game {

    /**
     *
     */
    constructor(ctx: CanvasRenderingContext2D) {
        super(ctx);
    }

    /**
     * @override
     */
    public render(): void {
        super.render();
        this.ctx.drawImage(ClientAssets.IMAGE_BACKGROUND, 0, 0);
    }

}