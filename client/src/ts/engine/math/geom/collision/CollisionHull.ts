import {IShape} from "../IShape";
import {Vector2D} from "../../Vector2D";

export interface CollisionHull extends IShape {

    /**
     * Gets the number of projection axes that are required by this collision hull.
     * Sometimes the number of projection axes of a collision hull depend on properties of the other collision hull being tested against.
     *
     * @param otherHull The collision hull being tested against.
     * @return The number of required projection axes.
     */
    getProjectionCount(otherHull: CollisionHull): number;

    /**
     * Generates projection axes facing away from this hull towards the given other hull.
     *
     * @param toOther A vector from this hull's reference frame to the other hull's reference frame.
     * @param otherHull The collision hull being tested against.
     * @return The array of axes.
     */
    getProjectionAxes(toOther: Vector2D, otherHull: CollisionHull): Vector2D[];

    /**
     * Projects the collision hull onto the given axis.
     *
     * @param location The location of the collision hull.
     * @param axis The axis to project the collision hull onto.
     * @return A Vector2D object representing the min and max range of the projection on the axis.
     */
    project(location: Vector2D, axis: Vector2D): Vector2D;

    /**
     * Gets the farthest point(s) of the CollisionHull in the direction of the vector.
     *
     * @param direction The direction in which to search.
     * @return An array of points on the hull that are the farthest in the given direction.
     */
    getFarthest(direction: Vector2D): Vector2D[];

}