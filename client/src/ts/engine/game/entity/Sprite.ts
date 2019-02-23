import {GameObject} from "./GameObject";
import {Vector2D} from "../../math/Vector2D";

/**
 * Single image `GameObject` implementation.
 */
export class Sprite extends GameObject {

    private readonly image: HTMLCanvasElement;

    /**
     * @param image The sprite's image.
     */
    constructor(uid: number, image: HTMLCanvasElement, location: Vector2D = Vector2D.ZERO) {
        super(uid, location);
        this.image = image;
    }

    /**
     * @return The image of the sprite.
     */
    public getImage(): HTMLCanvasElement {
        return this.image;
    }

    /**
     * @override
     */
    public render(ctx: CanvasRenderingContext2D): void {
        const loc: Vector2D = this.getLocation();
        ctx.drawImage(this.image, loc.x - this.image.width * 0.5, loc.y - this.image.height * 0.5);
    }

}
