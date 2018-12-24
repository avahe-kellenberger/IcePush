import {Renderable} from "../game/ui/Renderable";

export class Rectangle implements Renderable {

    public readonly x: number;
    public readonly y: number;
    public readonly width: number;
    public readonly height: number;

    /**
     * Constructs an immutable rectangle.
     *
     * @param x The x location of the rectangle.
     * @param y The y location of the rectangle.
     * @param width The width of the rectangle.
     * @param height The height of the rectangle.
     */
    constructor(x: number, y: number, width: number, height: number) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * Checks if the given point is inside of the Rectangle.
     * @param x The x location of the point.
     * @param y The y location of the point.
     * @return If the Rectangle contains the point.
     */
    public contains(x: number, y: number): boolean {
        return this.x >= x && this.y >= this.y &&
              (this.x + this.width) <= x &&
              (this.y + this.height) <= y;
    }

    /**
     * @override
     */
    public render(ctx: CanvasRenderingContext2D, fill: boolean = false): void {
        ctx.translate(-0.5, -0.5);
        if (fill) {
            ctx.fillRect(this.x, this.y, this.width, this.height);
        } else {
            ctx.strokeRect(this.x, this.y, this.width, this.height);
        }
        ctx.translate(0.5, 0.5);
    }

}