package rt.intersectables;

import java.util.ArrayList;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import rt.Material;
import rt.Ray;
import rt.Spectrum;
import rt.accelerators.BoundingBox;
import rt.materials.Diffuse;

public class CSGUnitCone extends CSGSolid {

	private CSGNode root;

	/**
	 * A finite cylinder centered around the z-axis, cut off at z=0 and z = height
	 * @param center
	 * @param radius
	 * @param height
	 */
	public CSGUnitCone(Material m) {
		float height = 1;
		CSGPlane top = new CSGPlane(new Vector3f(0,0.f,-1.f), -height);
		top.material = m;
		CSGPlane bottom = new CSGPlane(new Vector3f(0,0.f,1.f), 0);
		bottom.material = m;
		CSGInfiniteDoubleCone cone = new CSGInfiniteDoubleCone(m);
		CSGNode n1 = new CSGNode(top, cone, CSGNode.OperationType.INTERSECT);
		root = new CSGNode(n1, bottom, CSGNode.OperationType.INTERSECT);
	}

	public CSGUnitCone() {
		this(new Diffuse(new Spectrum(1,1,1)));
	}

	@Override
	ArrayList<IntervalBoundary> getIntervalBoundaries(Ray r) {
		return root.getIntervalBoundaries(r);
	}
	
	@Override
	public BoundingBox getBoundingBox() {
		return new BoundingBox(new Point3f(-1,-1,-1), new Point3f(1,1,1));
	}

}
