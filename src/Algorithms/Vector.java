package Algorithms;

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
}
