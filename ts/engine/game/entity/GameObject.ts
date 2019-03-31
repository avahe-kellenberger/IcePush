import {ILocatable, Locatable} from "./Locatable";
import {Entity} from "./Entity";
import {Vector2D} from "../../math/Vector2D";

/**
 * Invoked when the object's location changes.
 */
type LocationListener = (location: Vector2D) => void;

export abstract class GameObject implements ILocatable, Locatable, Entity {

    private readonly uid: number;
    private readonly locationListeners: Set<LocationListener>;
    private location: Vector2D;
    private velocity: Vector2D;

    /**
     * @param uid The object's unique ID number.
     * @param location The location of the object.
     */
    constructor(uid: number, location: Vector2D = Vector2D.ZERO) {
        this.uid = uid;
        this.locationListeners = new Set();
        this.location = location;
        this.velocity = Vector2D.ZERO;
    }

    /**
     * @return The object's unique ID number.
     */
    public getUID():number  {
        return this.uid;
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
    public setLocation(loc: Vector2D): boolean {
        if (this.location.equals(loc)) {
            return false;
        }
        this.location = loc;
        this.locationListeners.forEach(listener => listener(loc));
        return true;
    }

    /**
     * @override
     */
    public translate(delta: Vector2D): void {
        this.setLocation(this.location.addVector(delta));
    }

    /**
     * Adds a `LocationListener` to the object.
     * @param listener The listener to add.
     * @return If the listener was added.
     */
    public addLocationListener(listener: LocationListener): boolean {
        return this.locationListeners.size !== this.locationListeners.add(listener).size;
    }

    /**
     * @param listener The listener to check.
     * @return If the object contains the lister.
     */
    public containsLocationListener(listener: LocationListener): boolean {
        return this.locationListeners.has(listener);
    }

    /**
     * Removes a `LocationListener` from the object.
     * @param listener The listener to remove.
     * @return If the listener was removed.
     */
    public removeLocationListener(listener: LocationListener): boolean {
        return this.locationListeners.delete(listener);
    }

    /**
     * @return The object's velocity.
     */
    public getVelocity(): Vector2D {
        return this.velocity;
    }

    /**
     * Sets this object's velocity.
     * @param velocity The object's new velocity.
     * @return If the object's velocity was changed.
     */
    public setVelocity(velocity: Vector2D): boolean {
        if (this.velocity === velocity) {
            return false;
        }
        this.velocity = velocity;
        return true;
    }

    /**
     * @override
     */
    public update(delta: number): void {
        this.location = this.location.addVector(this.velocity.multiplyScalar(delta));
    }

    /**
     * @override
     */
    abstract render(ctx: CanvasRenderingContext2D): void;

}
