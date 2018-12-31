import {ILocatable, Locatable} from "./Locatable";
import {Entity} from "./Entity";
import {Vector2D} from "../../math/Vector2D";

export abstract class GameObject implements ILocatable, Locatable, Entity {

    private location: Vector2D;
    private velocity: Vector2D;

    /**
     * @param location The location of the object.
     */
    constructor(location: Vector2D = Vector2D.ZERO) {
        this.location = location;
        this.velocity = Vector2D.ZERO;
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
    public update(delta: number): void {
        this.location = this.location.addVector(this.velocity.multiplyScalar(delta));
    }

    /**
     * @override
     */
    abstract render(ctx: CanvasRenderingContext2D): void;

}
