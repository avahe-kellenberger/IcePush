import {CollisionHull} from "./CollisionHull";
import {Rectangle} from "../Rectangle";
import {Vector2D} from "../../Vector2D";
import {Circle} from "../Circle";

export class CircleCollisionHull implements CollisionHull {

    private readonly circle: Circle;

    /**
     * Creates a collision hull using the given circle.
     * @param circle The underlying circle of the hull.
     */
    constructor(circle: Circle) {
        this.circle = circle;
    }

    /**
     * @override
     */
    public getProjectionCount(otherShape: CollisionHull): number {
        if (otherShape instanceof CircleCollisionHull) {
            return 1;
        }
        return otherShape.getProjectionCount(this);
    }

    /**
     * @override
     */
    public getProjectionAxes(toOther: Vector2D, otherShape: CollisionHull): Vector2D[] {
        const projectionAxes: Vector2D[] = [];
        if (otherShape instanceof CircleCollisionHull) {
            projectionAxes.push(otherShape.getLocation()
                .subtractVector(this.getLocation())
                .addVector(toOther)
                .normalize());
        } else {
            throw new Error("Unexpected CollisionShape type.");
        }
        return projectionAxes;
    }

    /**
     * @override
     */
    public project(location: Vector2D, axis: Vector2D): Vector2D {
        const newLoc: Vector2D = this.getLocation().addVector(location);
        const centerDot: number = axis.dotProduct(newLoc);
        const radius: number = this.circle.getRadius();
        return new Vector2D(centerDot - radius, centerDot + radius);
    }

    /**
     * @override
     */
    public getFarthest(direction: Vector2D): Vector2D[] {
        return [this.getLocation().addVector(direction.normalize(this.circle.getRadius()))];
    }

    /**
     * @return The underlying circle of the hull.
     */
    public getCircle(): Circle {
        return this.circle;
    }

    /**
     * @override
     */
    public getBounds(): Rectangle {
        return this.circle.getBounds();
    }

    /**
     * @override
     */
    public getLocation(): Vector2D {
        return this.circle.getLocation();
    }

    /**
     * @override
     */
    public containsPoint(point: Vector2D): boolean {
        return this.circle.containsPoint(point);
    }

    /**
     * @override
     */
    public containsRectangle(rect: Rectangle): boolean {
        return this.circle.containsRectangle(rect);
    }

    /**
     * @override
     */
    public getArea(): number {
        return this.circle.getArea();
    }

    /**
     * @override
     */
    public intersects(rect: Rectangle): boolean {
        return this.circle.intersects(rect);
    }

    /**
     * @override
     */
    public render(ctx: CanvasRenderingContext2D, fill: boolean = false): void {
        this.circle.render(ctx, fill);
    }

}