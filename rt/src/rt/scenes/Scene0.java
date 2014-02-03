package rt.scenes;

import rt.*;
import rt.cameras.FixedCamera;
import rt.films.BoxFilterFilm;
import rt.integrators.PointLightIntegratorFactory;
import rt.intersectables.CSGDodecahedron;
import rt.intersectables.CSGNode;
import rt.intersectables.CSGPlane;
import rt.lightsources.PointLight;
import rt.samplers.OneSamplerFactory;
import rt.tonemappers.ClampTonemapper;

import javax.vecmath.*;

/**
 * Ray traces a simple scene. An educational example to show how to use the {@link rt} framework.
 */
public class Scene0 extends Scene {
		
	public Scene0()
	{
		// Output file name
		outputFilename = new String("..//output images//Scene0");
		
		// Image width and height in pixels
		width = 512;
		height = 512;
		
		// Number of samples per pixel
		SPP = 1;
		
		// Specify which camera, film, and tonemapper to use
		camera = new FixedCamera(width, height);
		film = new BoxFilterFilm(width, height);
		tonemapper = new ClampTonemapper();
		
		// Specify which integrator and sampler to use
		integratorFactory = new PointLightIntegratorFactory();
		samplerFactory = new OneSamplerFactory();
		
		// Define the root object (an intersectable) of the scene
		// Two CSG planes and a dodecahedron
		CSGNode n1 = new CSGNode(new CSGPlane(new Vector3f(0.f, 1.f, 0.f), 1.f), new CSGDodecahedron(), CSGNode.OperationType.ADD);
		root = new CSGNode(n1, new CSGPlane(new Vector3f(0.f, 0.f, 1.f), 1.f), CSGNode.OperationType.ADD); 
		
		// Light sources
		LightSource pointLight = new PointLight(new Vector3f(0.f, 0.f, 3.f), new Spectrum(4.f, 4.f, 4.f));
		lightList = new LightList();
		lightList.add(pointLight);
	}
	
}
