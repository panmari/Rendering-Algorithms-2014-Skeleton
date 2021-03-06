package rt.cameras;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import rt.Camera;
import rt.Ray;

public class PinholeCamera implements Camera {

	Matrix4f m = new Matrix4f();
	Vector3f eye;
	
	public PinholeCamera(Vector3f eye, Vector3f lookAt, Vector3f up, float fov,
			float aspect, int width, int height) {
		this.eye = eye;
		Vector3f w = new Vector3f();
		w.sub(eye, lookAt);
		w.normalize();
		Vector3f u = new Vector3f();
		u.cross(up, w);
		u.normalize();
		Vector3f v = new Vector3f();
		v.cross(w, u);
		
		m.setColumn(0, new Vector4f(u));
		m.setColumn(1, new Vector4f(v));
		m.setColumn(2, new Vector4f(w));
		m.setColumn(3, new Vector4f(eye));
		m.m33 = 1; // fourth column is actually a point
		
		// The viewport thingy
		Matrix4f p = new Matrix4f();
		float t = (float)Math.tan(Math.toRadians(fov)/2);
		float r = aspect*t;
		p.m00 = 2*r/width;
		p.m02 = r;
		p.m11 = 2*t/height;
		p.m12 = t;
		p.m22 = 1;
		p.m33 = 1;
		
		m.mul(p);
	}

	@Override
	public Ray makeWorldSpaceRay(int i, int j, float[] samples) {
		// Make point on image plane in viewport coordinates, that is range [0,width-1] x [0,height-1]
		// The assumption is that pixel [i,j] is the square [i,i+1] x [j,j+1] in viewport coordinates
		Vector4f d = new Vector4f(i+ samples[0], j + samples[1], -1, 1);
		
		// Transform it back to world coordinates
		m.transform(d);
		
		// Make ray consisting of origin and direction in world coordinates
		Vector3f dir = new Vector3f(d.x, d.y, d.z);
		dir.sub(eye);
		//TODO: somehow decide on time in a cleaner fashion
		return new Ray(new Vector3f(eye), dir, (float) Math.random());
	}

}
