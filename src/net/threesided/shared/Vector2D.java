package net.threesided.shared;

/**
 * Immutable two-dimensional vector class.
 */
public class Vector2D {

    public static final Vector2D ZERO = new Vector2D(0, 0);
    public static final Vector2D ONE = new Vector2D(1, 1);

    public final double x;
    public final double y;

    /**
     * Creates a new vector with the given x and y components.
     * @param x The x component.
     * @param y The y component.
     */
    public Vector2D(final double x, final double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * @param v The other vector.
     * @return The dot product of this vector and the given vector.
     */
    public double dot(final Vector2D v) {
        return this.x * v.x + this.y * v.y;
    }

    /**
     * Calculates the square of the vector's magnitude.
     * NOTE: This is faster than Vector2D{@link #getMagnitude()}.
     * @return The squared magnitude of the vector.
     */
    public double getMagnitudeSquared() {
        return this.x * this.x + this.y * this.y;
    }

    /**
     * Calculates the magnitude of the vector.
     * NOTE: This is slower than Vector2D{@link #getMagnitudeSquared()}.
     * @return The magnitude of the vector.
     */
    public double getMagnitude() {
        return Math.sqrt(this.getMagnitudeSquared());
    }

    /**
     * Calculates the distance between the local vector and the given vector, as points in 2D space.
     * @param v The other vector.
     * @return The distance between this vector and the given vector.
     */
    public double getDistance(Vector2D v) {
        return Math.sqrt((v.x - this.x) * (v.x - this.x)
                       + (v.y - this.y) * (v.y - this.y));
    }

    /**
     * Creates a new vector which is the sum of the local and given vectors.
     * @param v The other vector.
     * @return The current vector plus the given vector.
     */
    public Vector2D add(Vector2D v) {
        return new Vector2D(this.x + v.x, this.y + v.y);
    }

    /**
     * Creates a new vector which is the result of the given vector being subtracted from the local vector.
     * @param v The other vector.
     * @return The given vector subtracted from the local vector.
     */
    public Vector2D subtract(Vector2D v) {
        return new Vector2D(this.x - v.x, this.y - v.y);
    }

    /**
     * Creates a new vector which is the product of the local vector's components and the scalar.
     * @param scalar The scalar with which to multiply the vector's components.
     * @return The given vector multiplied by the scalar.
     */
    public Vector2D multiply(double scalar) {
        return new Vector2D(this.x * scalar, this.y * scalar);
    }

    /**
     * @return The normalized representation of the local vector.
     */
    public Vector2D normalize() {
        final double len = this.getMagnitude();
        if (len != 0.0) {
            return new Vector2D(this.x / len, this.y / len);
        }
        return Vector2D.ZERO;
    }

    @Override
    public String toString() {
        return "X: " + this.x + " Y: " + this.y;
    }

}
