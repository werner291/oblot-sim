package nl.tue.oblotsim.Util;

/**
 * A vector containing 2 doubles, used for example as position calculator
 * No function modifies the original vector!
 */
public class Vector {
    public static final Vector ZERO = new Vector(0.0, 0.0);
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
        return Vector.add(this, v);
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
        return Vector.sub(this, v);
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
        return Vector.mult(this, a);
    }

    /**
     * Scalar multiplication of a vector
     * @param v the vector to multiply
     * @param a the scalar to multiply with
     * @return the multiplied vector
     */
    public static Vector mult(Vector v, double a) {
        return new Vector(v.x*a, v.y*a);
    }

    /**
     * Returns the dot product of the two vectors
     */
    public double dot(Vector v) {
        return Vector.dot(this, v);
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
        return Vector.inv(this);
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
        return Vector.dist(this, v);
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

    /**
     * Calculate the length of this vector
     * @return the length of this vector
     */
    public double len() {
        return len(this);
    }

    /**
     * Calculate the length of a vector
     * @param a the vector to get the length of
     * @return the length of the vector a
     */
    public static double len(Vector a) {
        return dist(a, new Vector(0, 0));
    }

    /**
     * Calculate the smallest angle between this vector and another vector
     * @param a the second vector
     * @return the smallest angle between the two vectors. Positive if clockwise, negative if anticlockwise.
     */
    public double angle(Vector a) {
        return angle(this, a);
    }

    /**
     * Calculate the smallest angle between two vectors.
     * @param a the first vector
     * @param b the second vector
     * @return the smallest angle between the two vectors. Positive if anticlockwise, negative if clockwise.
     */
    public static double angle(Vector a, Vector b) {
        double dot = a.x*b.x + a.y*b.y;
        double det = a.x*b.y - a.y*b.x;
        return Math.atan2(det, dot);
    }

    /**
     * The angle between the lines ba and bc (in that order), when the vectors are regarded as points.
     * @param a the endpoint of the first line segment
     * @param b the point at which the line segments connect and at which we want to calculate the angle
     * @param c the endpoint of the second line segment
     * @return the angle abc between the line segments ba and bc. Positive if anticlockwise, negative if clockwise.
     */
    public static double angle(Vector a, Vector b, Vector c) {
        return angle(a.sub(b), c.sub(b));
    }

    /**
     * Assuming a vector from the origin to (x, y), rotate the vector anticlockwise around the origin
     * @param a the angle to rotate with, in radians
     * @return a new rotated vector
     */
    public Vector rotate(double a) {
        return Vector.rotate(this, a);
    }

    /**
     * Assuming a vector from the origin to (x, y), rotate the vector anticlockwise around the origin
     * @param v the vector to rotate
     * @param a the angle to rotate with, in radians
     * @return a new rotated vector
     */
    public static Vector rotate(Vector v, double a) {
        double newX = Math.cos(a) * v.x - Math.sin(a) * v.y;
        double newY = Math.sin(a) * v.x + Math.cos(a) * v.y;
        return new Vector(newX, newY);
    }

    /**
     * Rotate this vector anticlockwise around vector o.
     * @param a the angle to rotate with, in radians.
     * @param o the vector to rotate around
     * @return a new rotated vector
     */
    public Vector rotate(double a, Vector o) {
        return Vector.rotate(this, a, o);
    }

    /**
     * Rotate Vector v anticlockwise around vector o with a degrees.
     * @param v the vector to rotate
     * @param a the angle to rotate
     * @param o the vector to rotate around
     * @return the rotated vector
     */
    public static Vector rotate(Vector v, double a, Vector o) {
        return v.sub(o).rotate(a).add(o);
    }

    /**
     * Calculates the cross product or determinant of two vectors
     * @param a the vector to calculate the cross product with
     * @return the determinant of this and a
     */
    public double cross(Vector a) {
            return cross(this, a);
    }

    /**
     * Calculates the cross product or determinant of two vectors
     * @param a the first vector
     * @param b the second vector
     * @return the cross product of the two vectors
     */
    public static double cross(Vector a, Vector b) {
        return a.x * b.y - a.y * b.x;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof Vector) {
            Vector other = (Vector) obj;
            // equality with floating point correction
            return equalsWithinEpsilon(other, Config.EPSILON);
        } else {
            return false;
        }
    }

    public boolean equalsWithinEpsilon(Vector other, double epsilon) {
        return Math.abs(this.x - other.x) < epsilon && Math.abs(this.y - other.y) < epsilon;
    }

    @Override
    public int hashCode() {
        double roundedX = ((int)(this.x / Config.EPSILON) * Config.EPSILON);
        double roundedY = ((int)(this.y / Config.EPSILON) * Config.EPSILON);
        return Double.hashCode(roundedX) + Double.hashCode(roundedY);
    }

    @Override
    public String toString() {
        return String.format("Vector (%f; %f)", x, y);
    }

    public Vector normalized() {

        final double len = this.len();
        return len == 0 ? this : this.mult(1.0/ len);
    }

    public Vector restrictLength(double maxLength) {
        final double len = this.len();
        return len > maxLength ? this.mult(maxLength / len) : this;
    }
}
