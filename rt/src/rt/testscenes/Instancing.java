package rt.testscenes;

import java.io.IOException;

import rt.*;
import rt.cameras.*;
import rt.films.*;
import rt.integrators.*;
import rt.intersectables.*;
import rt.lightsources.*;
import rt.samplers.*;
import rt.tonemappers.*;
import rt.materials.*;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import org.omg.CORBA.TRANSACTION_MODE;

/**
 * Test scene for instancing and rendering triangle meshes.
 */
public class Instancing extends Scene {

	public IntersectableList objects;

	/**
	 * Timing: 8.5 sec on 12 core Xeon 2.5GHz, 24 threads
	 */
	public Instancing()
	{	
		outputFilename = new String("../output/testscenes/Instancing-mine");
		
		// Specify integrator to be used
		integratorFactory = new PointLightIntegratorFactory();
		//integratorFactory = new DebugIntegratorFactory();

		// Specify pixel sampler to be used
		samplerFactory = new OneSamplerFactory();
		
		// Make camera and film
		Vector3f eye = new Vector3f(0.f,0.f,2.f);
		Vector3f lookAt = new Vector3f(0.f,0.f,0.f);
		Vector3f up = new Vector3f(0.f,1.f,0.f);
		float fov = 60.f;
		int width = 256;
		int height = 256;
		float aspect = (float)width/(float)height;
		camera = new PinholeCamera(eye, lookAt, up, fov, aspect, width, height);
		film = new BoxFilterFilm(width, height);						
		tonemapper = new ClampTonemapper();
		
		// List of objects
		objects = new IntersectableList();	
				
		// Box
		Plane plane = new Plane(new Vector3f(0.f, 1.f, 0.f), 1.f);
		plane.material = new Diffuse(new Spectrum(0.f, 0.8f, 0.8f));
		objects.add(plane);		
		
		plane = new Plane(new Vector3f(0.f, 0.f, 1.f), 1.f);
		plane.material = new Diffuse(new Spectrum(0.3f, 0.8f, 0.8f));
		objects.add(plane);
		
		plane = new Plane(new Vector3f(-1.f, 0.f, 0.f), 1.f);
		plane.material = new Diffuse(new Spectrum(1.f, 0.8f, 0.8f));
		objects.add(plane);
		
		plane = new Plane(new Vector3f(1.f, 0.f, 0.f), 1.f);
		plane.material = new Diffuse(new Spectrum(0.f, 0.8f, 0.0f));
		objects.add(plane);
		
		plane = new Plane(new Vector3f(0.f, -1.f, 0.f), 1.f);
		plane.material = new Diffuse(new Spectrum(0.8f, 0.8f, 0.8f));
		objects.add(plane);
		
		// Add objects
		Intersectable mesh = null;
			
		//mesh = new CSGSphere(new Point3f(0,0,0), .3f);
		try {
			mesh = ObjReader.read("../obj/teapot.obj", 1.f);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Matrix4f t = new Matrix4f();
		t.setIdentity();
		
		// Instance one
		t.setScale(0.5f);
		t.setTranslation(new Vector3f(0.f, -0.35f, 0.f));
		Instance instance = new Instance(mesh, t);
		objects.add(instance);	
		
		// Instance two
		t = new Matrix4f();
		t.setIdentity();
		t.setScale(0.5f);
		t.setTranslation(new Vector3f(0.f, 0.25f, 0.f));
		Matrix4f rot = new Matrix4f();
		rot.rotX((float)Math.toRadians(30.f));
		t.mul(rot);
		instance = new Instance(mesh, t);
		objects.add(instance);
				
		root = objects;
		
		// List of lights
		lightList = new LightList();
		
		LightGeometry light = new PointLight(new Vector3f(0.f,0.8f,0.8f), new Spectrum(3.f, 3.f, 3.f));
		lightList.add(light);
		
		light = new PointLight(new Vector3f(-0.8f,0.2f,1.f), new Spectrum(1.5f, 1.5f, 1.5f));
		lightList.add(light);		
	}
}
