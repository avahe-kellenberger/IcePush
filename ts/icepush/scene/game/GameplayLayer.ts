import {Layer} from "../../../engine/game/Layer";
import {Rectangle} from "../../../engine/math/geom/Rectangle";
import {Vector2D} from "../../../engine/math/Vector2D";
import {Assets} from "../../asset/Assets";

export class GameplayLayer extends Layer {

    private readonly icePlatformBounds: Rectangle;

    constructor(zOrder: number = 1) {
        super(zOrder);
        /*
         * NOTE: These values were taken directly from the background image,
         * and assumes the canvas fits the same size.
         *
         * It would be wise in the future to have an actual GameArea object that is rendered up a generic background
         * image, which would prevent this poor coding style of hard-coding magic values.
         */
        this.icePlatformBounds = new Rectangle(new Vector2D(28, 30), 746, 424);
    }

    /**
     * @return The bounds of the ice platform in the game.
     */
    public getIcePlatformBounds(): Rectangle {
        return this.icePlatformBounds;
    }

    public render(ctx: CanvasRenderingContext2D): void {
        // Render the background image before the rest of the layer.
        ctx.drawImage(Assets.IMAGE_BACKGROUND, 0, 0);
        super.render(ctx);
    }

}