import {Scene} from "../../engine/game/Scene";
import {ClientAssets} from "../asset/ClientAssets";
import {Rectangle} from "../../engine/geom/Rectangle";
import {Game} from "../../engine/game/Game";

export class GameScene extends Scene {

    private gameArea: Rectangle;

    constructor(game: Game) {
        super(game);
        /*
         * NOTE: These values were taken directly from the background image,
         * and assumes the canvas fits the same size.
         *
         * It would be wise in the future to have an actual GameArea object that is rendered up a generic background
         * image, which would prevent this poor coding style of hard-coding magic values.
         */
        this.gameArea = new Rectangle(28, 30, 746, 424);
    }

    /**
     * @override
     */
    public render(ctx: CanvasRenderingContext2D): void {
        // Render the background image.
        ctx.drawImage(ClientAssets.IMAGE_BACKGROUND, 0, 0);
        super.render(ctx);
    }

    /**
     * @override
     */
    public onSwitchedToCurrent(): void {
        // TODO: Show DOM elements
    }

    /**
     * @override
     */
    public onSwitchedFromCurrent(): void {
        // TODO: Remove DOM elements
    }

}