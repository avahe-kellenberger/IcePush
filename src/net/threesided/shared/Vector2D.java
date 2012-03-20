package net.threesided.shared;

public class Vector2D {

    private double x;
    private double y;

    public Vector2D() {
        this.setX(0);
        this.setY(0);
    }

    public Vector2D(Vector2D v2) {
        this.setX(v2.getX());
        this.setY(v2.getY());
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getX() {
        return x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getY() {
        return y;
    }

    public void setD(double x, double y) {
        this.setX(x);
        this.setY(y);
    }

    public double dot(Vector2D v2) {
        return this.getX() * v2.getX() + this.getY() * v2.getY();
    }

    public double getLength() {
        return Math.sqrt(getX() * getX() + getY() * getY());
    }

    public double getDistance(Vector2D v2) {
        return Math.sqrt((v2.getX() - getX()) * (v2.getX() - getX()) + (v2.getY() - getY()) * (v2.getY() - getY()));
    }


    public Vector2D add(Vector2D v2) {
        setX(getX() + v2.getX());
        setY(getY() + v2.getY());
        return this;
    }

    public Vector2D subtract(Vector2D v2) {
        setX(this.getX() - v2.getX());
        setY(this.getY() - v2.getY());
        return this;
    }

    public Vector2D multiply(double scaleFactor) {
        setX(this.getX() * scaleFactor);
        setY(this.getY() * scaleFactor);
        return this;
    }

    public Vector2D normalize() {
        double len = getLength();
        if (len != 0.0) {
            this.setX(this.getX() / len);
            this.setY(this.getY() / len);
        } else {
            this.setX(0.0);
            this.setY(0.0);
        }
        return this;
    }

    public String toString() {
        return "X: " + getX() + " Y: " + getY();
    }
}

