import {Renderable} from "../../game/ui/Renderable";
import {Vector2D} from "../Vector2D";
import {Rectangle} from "./Rectangle";
import {ILocatable} from "../../game/entity/Locatable";

export interface IShape extends ILocatable, Renderable {

    /**
     * @return The bounds of the shape.
     */
    getBounds(): Rectangle;

    /**
     * @return The area of the shape.
     */
    getArea(): number;

    /**
     * @param point The point being checked.
     * @return If the shape contains the point.
     */
    containsPoint(point: Vector2D): boolean;

    /**
     * @param rect The Rectangle2D being checked.
     * @return If the shape entirely encompasses the rectangle.
     */
    containsRectangle(rect: Rectangle): boolean;

    /**
     * @param rect The Rectangle being checked.
     * @return If the shape intersects with the rectangle.
     */
    intersects(rect: Rectangle): boolean;

    /**
     * Renders the shape to the rendering context.
     * @param ctx The rendering context.
     * @param fill Whether the shape should be filled or stroked.
     */
    render(ctx: CanvasRenderingContext2D, fill?: boolean): void;

}