import {Vector2D} from "../Vector2D";
import {IShape} from "./IShape";

export class Rectangle implements IShape {

    private readonly topLeft: Vector2D;
    private readonly size: Vector2D;

    // Cached values
    private center: Vector2D|undefined;
    private bottomRight: Vector2D|undefined;

    /**
     * Constructs an immutable rectangle.
     *
     * @param topLeft The top-left location of the rectangle.
     * @param width The width of the rectangle.
     * @param height The height of the rectangle.
     */
    constructor(topLeft: Vector2D, width: number, height: number) {
        this.topLeft = topLeft;
        this.size = new Vector2D(width, height);
    }

    /**
     * @return The leftmost x coordinate of the rectangle.
     */
    public getMinX(): number {
        return this.topLeft.x;
    }

    /**
     * @return The x coordinate of the center of the rectangle.
     */
    public getCenterX(): number {
        return this.getLocation().x;
    }

    /**
     * @return The rightmost x coordinate of the rectangle.
     */
    public getMaxX(): number {
        return this.getMinX() + this.size.x;
    }

    /**
     * @return The topmost y coordinate of the rectangle.
     */
    public getMinY(): number {
        return this.topLeft.y;
    }

    /**
     * @return The y coordinate of the center of the rectangle.
     */
    public getCenterY(): number {
        return this.getLocation().y;
    }

    /**
     * @return The bottommost y coordinate of the rectangle.
     */
    public getMaxY(): number {
        return this.getMinY() + this.size.y;
    }

    /**
     * @return The top-left coordinate of the rectangle.
     */
    public getTopLeft(): Vector2D {
        return this.topLeft;
    }

    /**
     * @override
     */
    public getLocation(): Vector2D {
        if (this.center === undefined) {
            this.center = this.topLeft.add(this.size.x * 0.5, this.size.y * 0.5);
        }
        return this.center;
    }

    /**
     * Gets the bottom right coordinate of the Rectangle.
     * @returns {Vector2D}
     */
    public getBottomRight(): Vector2D {
        if (this.bottomRight == null) {
            this.bottomRight = this.topLeft.addVector(this.size);
        }
        return this.bottomRight;
    }

    /**
     * @returns If the width or height are less than or equal to zero.
     */
    public isEmpty(): boolean {
        return this.size.x <= 0 || this.size.y <= 0;
    }

    /**
     * Gets the Vectors representing each side of the Rectangle.
     * @returns {Vector2D[]}
     */
    public getVertices(): Vector2D[] {
        const topLeft = this.getTopLeft();
        const bottomRight = this.getBottomRight();
        return [
            topLeft,
            new Vector2D(bottomRight.x, topLeft.y),
            bottomRight,
            new Vector2D(topLeft.x, bottomRight.y)
        ];
    }

    /**
     * Checks if the given point is inside of the Rectangle.
     * @param point The point to check.
     * @return If the Rectangle contains the point.
     */
    public containsPoint(point: Vector2D): boolean {
        const thisX = this.getMinX();
        const thisY = this.getMinY();
        return point.x >= thisX &&
               point.y >= thisY &&
               point.x < thisX + this.size.x &&
               point.y < thisY + this.size.y;
    }

    /**
     * @override
     */
    public getArea(): number {
        return this.size.x * this.size.y;
    }

    /**
     * @override
     */
    public getBounds(): Rectangle {
        return this;
    }

    /**
     * @override
     */
    public containsRectangle(rect: Rectangle): boolean {
        const x = rect.getMinX();
        const y = rect.getMinY();
        const w = rect.size.x;
        const h = rect.size.y;
        if ((w <= 0) || (h <= 0) || this.isEmpty()) {
            return false;
        }
        const thisX = this.getMinX();
        const thisY = this.getMinY();
        return (x >= thisX) &&
            (y >= thisY) &&
            ((x + w) <= (thisX + this.size.x)) &&
            ((y + h) <= (thisY + this.size.y));
    }

    /**
     * @override
     */
    public intersects(rect: Rectangle): boolean {
        const x = rect.getMinX();
        const y = rect.getMinY();
        const w = rect.size.x;
        const h = rect.size.y;
        if (rect.isEmpty() || this.isEmpty()) {
            return false;
        }
        const thisX = this.getMinX();
        const thisY = this.getMinY();
        return (x + w > thisX) &&
            (y + h > thisY) &&
            (x < thisX + this.size.x) &&
            (y < thisY + this.size.y);
    }

    /**
     * @override
     */
    public render(ctx: CanvasRenderingContext2D, fill: boolean = false): void {
        const topLeft = this.topLeft;
        if (fill) {
            ctx.fillRect(topLeft.x, topLeft.y, this.size.x, this.size.y);
        } else {
            ctx.strokeRect(topLeft.x, topLeft.y, this.size.x, this.size.y);
        }
    }

}