package rt.testscenes;

import java.io.IOException;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import rt.*;
import rt.cameras.PinholeCamera;
import rt.films.BoxFilterFilm;
import rt.integrators.*;
import rt.intersectables.*;
import rt.lightsources.*;
import rt.materials.*;
import rt.samplers.*;
import rt.tonemappers.ClampTonemapper;

public class TextureTestScene extends Scene {
	
	public TextureTestScene()
	{
		// Output file name
		outputFilename = new String("../output/testscenes/TextureTestScene");
		
		// Image width and height in pixels
		width = 640;
		height = 360;
		
		// Number of samples per pixel
		SPP = 4;
		
		// Specify which camera, film, and tonemapper to use
		Vector3f eye = new Vector3f(0.f, 0.f, 5.f);
		Vector3f lookAt = new Vector3f(0.f, -.5f, 0.f);
		Vector3f up = new Vector3f(0.f, 1.f, 0.f);
		float fov = 60.f;
		float aspect = 16.f/9.f;
		camera = new PinholeCamera(eye, lookAt, up, fov, aspect, width, height);
		film = new BoxFilterFilm(width, height);
		tonemapper = new ClampTonemapper();
		
		// Specify which integrator and sampler to use
		integratorFactory = new PointLightIntegratorFactory();
		samplerFactory = new OneSamplerFactory();		
		
		Material chessTexture = new Textured("../textures/chessboard.jpg", "../normalmaps/normal.gif");
		Material forestfloor = new Textured("../textures/egg.jpg", "../normalmaps/forestfloor.jpg");

		Material couch = new Textured("../textures/pink.jpg", "../normalmaps/couch.png");

		CSGSolid sphere = new CSGSphere(chessTexture);
		CSGSolid cube = new CSGCube();
		
		Matrix4f t = new Matrix4f();
		t.setIdentity();
		t.rotX((float) Math.toRadians(30));
		t.setTranslation(new Vector3f(2.5f,0,0));
		CSGInstance cubeInstance = new CSGInstance(cube, t);
		cubeInstance.material = couch;
		
		// Ground and back plane
		XYZGrid grid = new XYZGrid(new Spectrum(0.2f, 0.f, 0.f), new Spectrum(1.f, 1.f, 1.f), 0.1f, new Vector3f(0.f, 0.3f, 0.f));
		CSGPlane groundPlane = new CSGPlane(new Vector3f(0.f, 1.f, 0.f), 1.5f);
		groundPlane.material = forestfloor;
		CSGPlane backPlane = new CSGPlane(new Vector3f(0.f, 0.f, 1.f), 3.15f);
		backPlane.material = grid;		
		
		// Collect objects in intersectable list
		IntersectableList intersectableList = new IntersectableList();
		intersectableList.add(sphere);	
		intersectableList.add(cubeInstance);
		intersectableList.add(groundPlane);
		intersectableList.add(backPlane);
		
		// Set the root node for the scene
		root = intersectableList;
		
		// Light sources
		Vector3f lightPos = new Vector3f(eye);
		lightPos.add(new Vector3f(-1.f, 0.f, 0.f));
		LightGeometry pointLight1 = new PointLight(lightPos, new Spectrum(14.f, 14.f, 14.f));
		lightPos.add(new Vector3f(2.f, 0.f, 0.f));
		LightGeometry pointLight2 = new PointLight(lightPos, new Spectrum(14.f, 14.f, 14.f));
		LightGeometry pointLight3 = new PointLight(new Vector3f(0.f, 5.f, 1.f), new Spectrum(24.f, 24.f, 24.f));
		lightList = new LightList();
		lightList.add(pointLight1);
		lightList.add(pointLight2);
		lightList.add(pointLight3);
	}
}
