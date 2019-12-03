package Algorithms;

/**
 * A vector containing 2 doubles, used for example as position calculator
 * ALL FUNCTIONS MODIFY THE ORIGINAL VECTOR!
 */
public class Vector {
    public double x;
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
     * @return the combined vector.
     */
    public Vector add(Vector v) {
        this.x += v.x;
        this.y += v.y;
        return this;
    }

    /**
     * Scalar multiplication of a vector
     * @param a the scalar to multiply with
     * @return the multiplied vector
     */
    public Vector mult(double a) {
        this.x *= a;
        this.y *= a;
        return this;
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
}
