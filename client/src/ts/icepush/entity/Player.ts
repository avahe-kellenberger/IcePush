import {GameObject} from "../../engine/game/entity/GameObject";
import {Vector2D} from "../../engine/math/Vector2D";

export class Player extends GameObject {

    private readonly name: string;
    private readonly sprite: HTMLImageElement;

    /**
     * Creates a new player.
     * @param name The player's name.
     * @param location The player's initial location.
     * @param sprite The player's sprite.
     */
    constructor(name: string, location: Vector2D, sprite: HTMLImageElement) {
        super(location);
        this.name = name;
        this.sprite = sprite;
    }

    /**
     * @override
     */
    public render(ctx: CanvasRenderingContext2D): void {
        const center: Vector2D = this.getLocation();
        const top: number = center.y - this.sprite.height * 0.5;
        ctx.drawImage(this.sprite, center.x - this.sprite.width * 0.5, top);

        ctx.fillStyle = 'red';
        const fontSize: number = 14;
        ctx.font = `${fontSize}px Arial`;
        const metrics: TextMetrics = ctx.measureText(this.name);

        /*
         * See https://developer.mozilla.org/en-US/docs/Web/CSS/line-height#Values for an explanation.
         * 'Desktop browsers (including Firefox) use a default value of roughly 1.2, depending on the element's font-family.'
         * We multiply by '0.6' to halve the height.
         */
        const height = fontSize * 0.6;
        ctx.fillText(this.name, center.x - metrics.width * 0.5, top - height);
    }

}