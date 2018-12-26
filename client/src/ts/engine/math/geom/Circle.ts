import {IShape} from "./IShape";
import {Vector2D} from "../Vector2D";
import {Rectangle} from "./Rectangle";

export class Circle implements IShape {

    private readonly radius: number;
    private bounds: Rectangle|undefined;
    private readonly location: Vector2D;

    /**
     *
     * @param radius
     * @param location
     */
    constructor(radius: number, location: Vector2D = Vector2D.ZERO) {
        this.radius = radius;
        this.location = location;
    }

    /**
     * @return The radius of the circle.
     */
    public getRadius(): number {
        return this.radius;
    }

    /**
     * @override
     */
    public getBounds(): Rectangle {
        if (this.bounds === undefined) {
            const diameter: number = this.radius * 2;
            this.bounds = new Rectangle(this.location.subtract(this.radius, this.radius), diameter, diameter);
        }
        return this.bounds;
    }

    /**
     * @override
     */
    public getArea(): number {
        return Math.PI * this.radius * this.radius;
    }

    /**
     * @override
     */
    public containsPoint(point: Vector2D): boolean {
        return this.location.distanceSquared(point) < (this.radius * this.radius);
    }

    /**
     * @override
     */
    public containsRectangle(rect: Rectangle): boolean {
        for (const vert of rect.getVertices()) {
            if (!this.containsPoint(vert)) {
                return false;
            }
        }
        return true;
    }

    /**
     * @override
     */
    public intersects(rect: Rectangle): boolean {
        for (let vert of rect.getVertices()) {
            if (this.containsPoint(vert)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @override
     */
    public getLocation(): Vector2D {
        return this.location;
    }

    /**
     * @override
     */
    public render(ctx: CanvasRenderingContext2D, fill?: boolean): void {
        ctx.beginPath();
        ctx.arc(this.location.x, this.location.y, this.radius, 0, Math.PI * 2);
        ctx.closePath();
        if (fill) {
            ctx.fill();
        } else {
            ctx.stroke();
        }
    }

}