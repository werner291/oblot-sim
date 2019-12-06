package Algorithms;

import java.util.Comparator;

/**
 * A vector containing 2 doubles, used for example as position calculator
 * No function modifies the original vector!
 */
public class Vector {
    /**
     * The x coordinate of the vector.
     */
    public double x;
    /**
     * The y coordinate of the vector.
     */
    public double y;

    /**
     * Constructs a normal vector
     */
    public Vector(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * A copy constructor
     */
    public Vector(Vector v) {
        this.x = v.x;
        this.y = v.y;
    }

    /**
     * Add a vector
     * @param v the vector to add
     * @return this+v
     */
    public Vector add(Vector v) {
        return new Vector(this.x+v.x, this.y+v.y);
    }

    /**
     * Add two vectors
     * @param a first vector
     * @param b second vector
     * @return a+b
     */
    public static Vector add(Vector a, Vector b) {
        return new Vector(a.x+b.x, a.y+b.y);
    }

    /**
     * Subtract v from this vector
     * @param v the vector to subtract
     * @return this-v
     */
    public Vector sub(Vector v) {
        return new Vector(this.x - v.x, this.y - v.y);
    }

    /**
     * Subtract two vectors
     * @param a first vector
     * @param b second vector
     * @return a-b
     */
    public static Vector sub(Vector a, Vector b) {
        return new Vector(a.x - b.x, a.y - b.y);
    }

    /**
     * Scalar multiplication of a vector
     * @param a the scalar to multiply with
     * @return the multiplied vector
     */
    public Vector mult(double a) {
        return new Vector(this.x*a, this.y*a);
    }

    /**
     * Returns the dot product of the two vectors
     */
    public double dot(Vector v) {
        return this.x * v.x + this.y * v.y;
    }

    /**
     * Returns the dot product of the two vectors
     */
    public static double dot (Vector a, Vector b) {
        return a.x * b.x + a.y * b.y;
    }

    /**
     * Return the inverse of this vector, i.e. the negation
     * @return the inverse of this vector
     */
    public Vector inv() {
        return new Vector(-this.x, -this.y);
    }

    /**
     * Return the inverse of this vector, i.e. the negation
     * @param v the vector to get the inverse from
     * @return the inverse of this vector
     */
    public static Vector inv(Vector v) {
        return new Vector(-v.x, -v.y);
    }

    /**
     * Calculate the distance between two points when the vectors are regarded as points.
     * @param v the vector to get the distance with.
     * @return the euclidian distance to another point
     */
    public double dist(Vector v) {
        double distX = Math.abs(v.x - this.x);
        double distY = Math.abs(v.y - this.y);
        return Math.sqrt(distX*distX + distY*distY);
    }

    /**
     * Calculate the distance between two points when the vectors are regarded as points.
     * @param a the first point
     * @param b the second point
     * @return the euclidean distance between a and b
     */
    public static double dist(Vector a, Vector b) {
        double distX = Math.abs(a.x - b.x);
        double distY = Math.abs(a.y - b.y);
        return Math.sqrt(distX*distX + distY*distY);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof Vector) {
            Vector other = (Vector) obj;
            return this.x == other.x && this.y == other.y; //TODO: floating point correction
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Double.hashCode(this.x) + Double.hashCode(this.y);
    }

    @Override
    public String toString() {
        return String.format("Vector (%f; %f)", x, y);
    }
}
