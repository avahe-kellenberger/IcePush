/**
 * An immutable two dimensional vector.
 */
export class Vector2D {

    public static readonly ZERO: Vector2D = new Vector2D(0, 0);
    public static readonly ONE: Vector2D = new Vector2D(1, 1);

    public readonly x: number;
    public readonly y: number;

    /**
     * Symbolizes an immutable vector with two dimensions.
     * @param x The x component.
     * @param y The y component.
     */
    constructor(x: number, y: number) {
        this.x = x;
        this.y = y;
    }

    /**
     * Gets the magnitude of this vector.
     * This function is slower than {@link getMagnitudeSquared()}.
     * @return The magnitude of this vector.
     */
    public getMagnitude(): number {
        return Math.sqrt(this.getMagnitudeSquared());
    }

    /**
     * Gets the squared magnitude of this vector.
     * This function is faster than {@link getMagnitude()}.
     * @return The squared magnitude of this vector.
     */
    public getMagnitudeSquared(): number {
        return (this.x * this.x) + (this.y * this.y);
    }

    /**
     * @param v The vector to add.
     * @return A new vector with the sum both objects' components.
     */
    public addVector(v: Vector2D): Vector2D {
        return this.add(v.x, v.y);
    }

    /**
     * @param x The x component.
     * @param y The y component.
     * @return A new vector with this vector's components added to the given components.
     */
    public add(x: number, y: number): Vector2D {
        return new Vector2D(this.x + x, this.y + y);
    }

    /**
     * @param v The vector to subtract.
     * @return A new vector which is the difference between this vector and the given vector.
     */
    public subtractVector(v: Vector2D): Vector2D {
        return this.subtract(v.x, v.y);
    }

    /**
     * @param x The x component.
     * @param y The y component.
     * @return A new vector with the given components subtracted from this vector's components.
     */
    public subtract(x: number, y: number): Vector2D {
        return new Vector2D(this.x - x, this.y - y);
    }

    /**
     * @param scalar The amount to scale the components of the vector.
     * @return A new vector that has been scaled.
     */
    public multiplyScalar(scalar: number): Vector2D {
        return new Vector2D(this.x * scalar, this.y * scalar);
    }

    /**
     * @param v the referenced Vector.
     * @return The dot product of this vector and the given vector.
     */
    public dotProduct(v: Vector2D): number {
        return (this.x * v.x) + (this.y * v.y);
    }

    /**
     * @param v the Vector being multiplied by.
     * @return The cross product of this vector and the given vector.
     */
    public crossProduct(v: Vector2D): number {
        return (this.x * v.y) - (v.x * this.y);
    }

    /**
     * @return This vector reflected off a normal.
     */
    public reflect(normal: Vector2D): Vector2D {
        let scalar: number = 2.0 * this.dotProduct(normal);
        return this.subtractVector(normal.multiplyScalar(scalar));
    }

    /**
     * @return A new vector with the negated values of this vector.
     */
    public negate(): Vector2D {
        return new Vector2D(-this.x, -this.y);
    }

    /**
     * @return A vector with inverted components of this vector.
     */
    public inverse(): Vector2D {
        return new Vector2D(1.0 / this.x, 1.0 / this.y);
    }

    /**
     * @return A copy of this vector with both components rounded to the nearest integer.
     */
    public round(): Vector2D {
        //noinspection JSSuspiciousNameCombination
        return new Vector2D(Math.round(this.x), Math.round(this.y));
    }

    /**
     * @return A copy of the absolute value of this vector.
     */
    public abs(): Vector2D {
        //noinspection JSSuspiciousNameCombination
        return new Vector2D(Math.abs(this.x), Math.abs(this.y));
    }

    /**
     * @return The value of the smaller component of this vector.
     */
    public min(): number {
        return Math.min(this.x, this.y);
    }

    /**
     * @return The value of the larger component of this vector.
     */
    public max(): number {
        return Math.max(this.x, this.y);
    }

    /**
     * Gets the perpendicular version of this vector.
     * This perpendicular vector faces to the right of `this` vector.
     * @return A perpendicular vector to this vector.
     */
    public getPerpendicular(): Vector2D {
        //noinspection JSSuspiciousNameCombination
        return new Vector2D(-this.y, this.x);
    }

    /**
     * Gets a normalized copy of this vector.
     * A normalized vector has a magnitude of 1.0 but still points in the same direction as the original.
     * @param magnitude The magnitude to normalize the vector's magnitude to.
     * @returns A normalized copy of this vector.
     */
    public normalize(magnitude: number = 1.0): Vector2D {
        const scale = magnitude / this.getMagnitude();
        return new Vector2D(this.x * scale, this.y * scale);
    }

    /**
     * Gets the distance to the given point.
     * This function is slower than {@link distanceSquared(Vector2D)}.
     * @param point The target point.
     */
    public distance(point: Vector2D): number {
        return Math.sqrt(this.distanceSquared(point));
    }

    /**
     * Gets the squared distance to the given point.
     * This function is faster than {@link distance(Vector2D)}.
     * @param point The target point.
     */
    public distanceSquared(point: Vector2D): number {
        const dX = this.x - point.x;
        const dY = this.y - point.y;
        return dX * dX + dY * dY;
    }

    /**
     * Gets the angle (in radians from -pi to pi) of the vector.
     */
    public getAngle(): number {
        return Math.atan2(this.y, this.x);
    }

    /**
     * Returns the components as a String.
     * @returns {string}
     */
    public toString(): string {
        return '(' + this.x + ',' + this.y + ')';
    }

    /**
     * @param v The vector to check.
     * @returns If the values of this vector are equal to the values of the given vector.
     */
    public equals(v: Vector2D): boolean {
        return this.x === v.x && this.y === v.y;
    }

}
