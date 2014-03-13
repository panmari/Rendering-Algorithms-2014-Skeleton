package rt;

import javax.vecmath.*;

/**
 * A ray represented by an origin and a direction.
 */
public class Ray {

	public Vector3f origin;
	public Vector3f direction;
	public int depth;
	
	/**
	 * Stores copies of the given tuples
	 * @param origin
	 * @param direction
	 */
	public Ray(Tuple3f origin, Tuple3f direction)
	{
		this(origin, direction, 0);
	}
	
	/**
	 * Stores copies of the given tuples
	 * @param origin
	 * @param direction
	 */
	public Ray(Tuple3f origin, Tuple3f direction, int depth)
	{
		this(origin, direction, 0, false);
	}
	
	/**
	 * Stores copies of the given tuples
	 * @param origin
	 * @param direction
	 */
	public Ray(Tuple3f origin, Tuple3f direction, int depth, boolean epsilon)
	{
		Vector3f o = new Vector3f();
		if (epsilon) {
			o.scaleAdd(1e-5f, direction, origin);
		} else
			o.set(origin);
		this.origin = o; 
		this.direction = new Vector3f(direction);
		this.depth = depth;
	}

	public Point3f pointAt(float t) {
		Point3f p = new Point3f(direction);
		p.scaleAdd(t, origin);
		return p;
	}
	
	public String toString() {
		return "orig: " + origin + " dir: " + direction;
	}
}
