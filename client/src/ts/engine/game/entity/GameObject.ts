import {ILocatable, Locatable} from "./Locatable";
import {Entity} from "./Entity";
import {Vector2D} from "../../math/Vector2D";
import {Collidable, ICollidable} from "./Collidable";
import {CollisionHull} from "../../math/geom/collision/CollisionHull";

export abstract class GameObject implements ILocatable, Locatable, Collidable, ICollidable, Entity {

    private location: Vector2D;
    private velocity: Vector2D;
    private hull: CollisionHull | undefined;

    /**
     * @param location The location of the object.
     * @param hull The object's hull used for collision detection.
     */
    constructor(location: Vector2D, hull?: CollisionHull) {
        this.location = location;
        this.velocity = Vector2D.ZERO;
        this.hull = hull;
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
        return true;
    }

    /**
     * @override
     */
    public translate(delta: Vector2D): void {
        this.location = this.location.addVector(delta);
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
    public getCollisionHull(): CollisionHull | undefined {
        return this.hull;
    }

    /**
     * @override
     */
    public setCollisionHull(hull: CollisionHull | undefined): boolean {
        if (this.hull === hull) {
            return false;
        }
        this.hull = hull;
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
