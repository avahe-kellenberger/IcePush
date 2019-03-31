/**
 * Z-order is used to sort objects for rendering. The specification is as follows:
 *
 * - Comparing two objects, the object with the lower z-order will be rendered first.
 * - Objects with the same z-order could be rendered in any order.
 * - Z-order does not have any sort of 3D rendering affect; it is merely to determine the order of rendering 2D objects
 *   and can be conceptualized as having "layers" upon which objects are rendered to the display.
 */
export interface ZOrder {

    /**
     * @return The z-order of the object.
     */
    getZOrder(): number;

    /**
     * Sets the object's z-order.
     * @param zOrder The object's z-order.
     */
    setZOrder(zOrder: number): void;

}