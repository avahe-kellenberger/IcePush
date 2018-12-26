import {Vector2D} from "../../math/Vector2D";

/**
 * The mutable properties of an object with a location.
 */
export interface Locatable {

    /**
     * @param loc The center location of the object.
     * @return If the location of the object was changed.
     */
    setLocation(loc: Vector2D): boolean;

    /**
     * Moves the object by the value given.
     * @param delta The delta vector.
     */
    translate(delta: Vector2D): void;

}

/**
 * An immutable interface for read-only details of an object with a location.
 */
export interface ILocatable {

    /**
     * @return The center location of the object.
     */
    getLocation(): Vector2D;

}
