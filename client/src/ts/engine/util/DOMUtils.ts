import {Vector2D} from "../math/Vector2D";

export class DOMUtils {

    /**
     * @param element The element to check.
     * @param point The point to check.
     * @param offset The amount to offset the element for the point check.
     * @return If the point is within the bounds of the element.
     */
    public static containsPoint(element: HTMLElement, point: Vector2D, offset: Vector2D = Vector2D.ZERO): boolean {
        point = point.addVector(offset);

        const elementBounds: ClientRect|DOMRect = element.getBoundingClientRect();
        const top: number = elementBounds.top;
        const bottom: number = elementBounds.bottom;
        if (top > point.y || point.y > bottom) {
            return false;
        }

        const left: number = elementBounds.left;
        const right: number = elementBounds.right;
        return left <= point.x && point.x <= right;
    }

}