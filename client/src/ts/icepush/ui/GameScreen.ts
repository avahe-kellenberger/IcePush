import {Scene} from "../../engine/game/Scene";
import {ClientAssets} from "../asset/ClientAssets";

export class GameScreen extends Scene {

    /**
     * @override
     */
    public render(ctx: CanvasRenderingContext2D): void {
        // Render the background image.
        ctx.drawImage(ClientAssets.IMAGE_BACKGROUND, 0, 0);
        super.render(ctx);
    }

}