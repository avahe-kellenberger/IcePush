import {CollisionHull} from "../../math/geom/collision/CollisionHull";

/**
 * The mutable properties of a collidable object.
 */
export interface Collidable {
    /**
     * Sets the object's collision hull.
     * @param hull The object's new hull.
     * @return If the hull was changed.
     */
    setCollisionHull(hull: CollisionHull|undefined): boolean;
}

/**
 * The immutable properties of a collidable object.
 */
export interface ICollidable {
    /**
     * @return The object's collision hull.
     */
    getCollisionHull(): CollisionHull|undefined;
}