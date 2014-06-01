package rt.integrators;

import java.util.Iterator;

import javax.vecmath.*;

import rt.HitRecord;
import rt.Integrator;
import rt.Intersectable;
import rt.LightList;
import rt.LightGeometry;
import rt.Ray;
import rt.Sampler;
import rt.Scene;
import rt.Spectrum;
import util.ImprovedNoise;
import util.MyMath;
import util.StaticVecmath;

/**
 * Integrator for Whitted style ray tracing. This is a basic version that needs to be extended!
 */
public class PointLightIntegrator implements Integrator {

	LightList lightList;
	Intersectable root;
	
	public PointLightIntegrator(Scene scene)
	{
		this.lightList = scene.getLightList();
		this.root = scene.getIntersectable();
	}

	/**
	 * Basic integrator that simply iterates over the light sources and accumulates
	 * their contributions. No shadow testing, reflection, refraction, or 
	 * area light sources, etc. supported.
	 */
	public Spectrum integrate(Ray r) {

		HitRecord hitRecord = root.intersect(r);
		if(hitRecord != null)
		{
			Spectrum outgoing = new Spectrum(0.f, 0.f, 0.f);
			Spectrum brdfValue;
			// Iterate over all light sources
			Iterator<LightGeometry> it = lightList.iterator();
			while(it.hasNext())
			{
				LightGeometry lightSource = it.next();
				
				// Make direction from hit point to light source position; this is only supposed to work with point lights
				HitRecord lightHit = lightSource.sample(null);
				Vector3f lightDir = StaticVecmath.sub(lightHit.position, hitRecord.position);
				float d2 = lightDir.lengthSquared();
				lightDir.normalize();
				
				Ray shadowRay = new Ray(hitRecord.position, lightDir, r.t, 0, true);
				HitRecord shadowHit = root.intersect(shadowRay);
				if (shadowHit != null &&
						StaticVecmath.dist2(shadowHit.position, hitRecord.position) < d2) //only if closer than light
					continue;
				
				// Evaluate the BRDF
				brdfValue = hitRecord.material.evaluateBRDF(hitRecord, hitRecord.w, lightDir);
				
				// Multiply together factors relevant for shading, that is, brdf * emission * ndotl * geometry term
				Spectrum s = new Spectrum(brdfValue);
				
				// Multiply with emission
				s.mult(lightHit.material.evaluateEmission(lightHit, StaticVecmath.negate(lightDir)));
				
				// Multiply with cosine of surface normal and incident direction
				float ndotl = hitRecord.normal.dot(lightDir);
				ndotl = Math.max(ndotl, 0.f);
				s.mult(ndotl);
				
				// Geometry term: multiply with 1/(squared distance), only correct like this 
				// for point lights (not area lights)!
				s.mult(1.f/d2);
				
				// Accumulate
				outgoing.add(s);
			}
			Spectrum T = new Spectrum(1);
			Spectrum L = new Spectrum(0);
			float dist = MyMath.sqrt(StaticVecmath.dist2(r.origin, hitRecord.position));
			float ds = dist/100;
			HitRecord lightHit = lightList.get(0).sample(null);
			float sigma = 0.2f;
			Spectrum L_ve = new Spectrum(0.005f);
			for (float s_i = ds; s_i <= dist; s_i += ds) {
				
				Point3f p = r.pointAt(s_i); //asserts r.dir is normalized!!!
				Spectrum inscattering = new Spectrum(T);
				
				Vector3f lightDir = StaticVecmath.sub(lightHit.position, p);
				float d2 = lightDir.lengthSquared();
				lightDir.normalize();
				Ray shadowRay = new Ray(p, lightDir, r.t, 0, true);
				HitRecord shadowHit = root.intersect(shadowRay);
				if (shadowHit != null &&
						StaticVecmath.dist2(shadowHit.position, hitRecord.position) < d2) //only if closer than light
					inscattering.mult(0);
				else {
					inscattering.mult(L_ve); //not in shadow
					Spectrum l = lightHit.material.evaluateEmission(lightHit, StaticVecmath.negate(lightDir));
					//TODO: do ray marching on shadow ray for transmittance?
					l.mult(MyMath.powE(-sigma*MyMath.sqrt(d2)));
					inscattering.mult(l);
				}
				
				L.add(inscattering);
				float sigma_s = sigmaS(p);
				
				T.mult(1 - sigma_s*ds);
			}
			L.mult(ds);
			outgoing.mult(T); //times surface reflection L_s
			L.add(outgoing);
			return L;
			//return outgoing;
		} else 
			return new Spectrum(0.f,0.f,0.f);
		
	}
	
	/**
	 * For now only float is returned, but could be spectrum
	 * @param p
	 * @return
	 */
	public float sigmaS(Point3f p){
		p.scale(20);
		float s = (float)(ImprovedNoise.noise(p.x, p.y, p.z) + 1)/5; // sigma at the current point p
		return s;
	}

	public float[][] makePixelSamples(Sampler sampler, int n) {
		return sampler.makeSamples(n, 2);
	}

}
