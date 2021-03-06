package rt;

import javax.imageio.ImageIO;

import rt.testscenes.*;
import util.HistHelper;
import util.ImageWriter;

import java.util.*;
import java.awt.Point;
import java.awt.image.*;
import java.io.*;

/**
 * The main rendering loop. Provides multi-threading support. The {@link Main#scene} to be rendered
 * is hard-coded here, so you can easily change it. The {@link Main#scene} contains 
 * all configuration information for the renderer.
 */
public class Main {

	/** 
	 * The scene to be rendered.
	 */
	public static Scene scene = new BiPathtracingBoxSphere();
	public static Point debugPixel;// = new Point(47, 475);
	public static final int windowSize = 10;
	
	static LinkedList<RenderTask> queue;
	static Counter tasksLeft;
		
	static public class Counter
	{
		public Counter(int n)
		{
			this.n = n;
		}
		
		public int n;
	}
	
	/**
	 * A render task represents a rectangular image region that is rendered
	 * by a thread in one chunk.
	 */
	static public class RenderTask
	{
		public int left, right, bottom, top;
		public Integrator integrator;
		public Scene scene;
		public Sampler sampler;
		
		public RenderTask(Scene scene, int left, int right, int bottom, int top)
		{			
			this.scene = scene;
			this.left = left;
			this.right = right;
			this.bottom = bottom;
			this.top = top;

			// The render task has its own sampler and integrator. This way threads don't 
			// compete for access to a shared sampler/integrator, and thread contention
			// can be reduced. 
			integrator = scene.getIntegratorFactory().make(scene);
			sampler = scene.getSamplerFactory().make();
			sampler.init(left*scene.height + bottom);
		}
	}
	
	static public class RenderThread implements Runnable
	{			
		public void run()
		{
			while(true)
			{
				RenderTask task;
				synchronized(queue)
				{
					if(queue.size() == 0) break;
					task = queue.poll();
				}
													
				// Render the image block represented by the task
				
				// For all pixels
				for(int j=task.bottom; j<task.top; j++)
				{
					for(int i=task.left; i<task.right; i++)
					{											
						float samples[][] = task.integrator.makePixelSamples(task.sampler, task.scene.getSPP());
						//for going in a s through pixels, adapt i here
						int iAdapted;
						if (j % 2 == 1)
							iAdapted = task.right + task.left - i - 1;
						else
							iAdapted = i;
						// For all samples of the pixel
						for(int k = 0; k < samples.length; k++)
						{	
							// Make ray
							Ray r = task.scene.getCamera().makeWorldSpaceRay(iAdapted, j, samples[k]);

							// Evaluate ray0
							Spectrum s = task.integrator.integrate(r);							
							
							// Write to film
							task.scene.getFilm().addSample(iAdapted + samples[k][0], j + samples[k][1], s);
						}
					}
				}
				
				synchronized(tasksLeft)
				{
					tasksLeft.n--;
					if(tasksLeft.n == 0) tasksLeft.notifyAll();
				}
			}
		}
	}
	
	public static void main(String[] args)
	{			
		int taskSize = 32;	// Each task renders a square image block of this size
		int nThreads; 
		if (debugPixel == null)
			nThreads = 4;
		else
			nThreads = 1;	// Number of threads to be used for rendering
				
		int width = scene.getFilm().getWidth();
		int height = scene.getFilm().getHeight();

		scene.prepare();
		
		int nTasks;
		queue = new LinkedList<RenderTask>();
		// Make render tasks, split image into blocks to be rendered by the tasks
		if (debugPixel != null) {
			scene.outputFilename += "_DEBUG";
			nTasks = 1;
			RenderTask debugTask = new RenderTask(scene, debugPixel.x - windowSize, debugPixel.x + 1 + windowSize, 
														 debugPixel.y - windowSize, debugPixel.y + 1 + windowSize);
			queue.add(debugTask);
		} else {
			nTasks = (int)Math.ceil((double)width/(double)taskSize) * (int)Math.ceil((double)height/(double)taskSize);
			for(int j=0; j<(int)Math.ceil((double)height/(double)taskSize); j++) {
				for(int i=0; i<(int)Math.ceil((double)width/(double)taskSize); i++) {
					RenderTask task = new RenderTask(scene, i*taskSize, Math.min((i+1)*taskSize, width), j*taskSize, 
																		Math.min((j+1)*taskSize, height));
					queue.add(task);
				}
			}
		}
		tasksLeft = new Counter(nTasks);

		Timer timer = new Timer();
		timer.reset();
		
		// Start render threads
		for(int i=0; i<nThreads; i++)
		{
			new Thread(new RenderThread()).start();
		}
		
		// Wait for threads to end
		int printed = 0;
		System.out.printf("Rendering scene %s to file %s: \n", scene.getClass().toString(), scene.outputFilename);
		System.out.printf("0%%                                                50%%                                           100%%\n");
		System.out.printf("|---------|---------|---------|---------|---------|---------|---------|---------|---------|---------\n");
		synchronized(tasksLeft)
		{
			while(tasksLeft.n>0)
			{
				try
				{
					tasksLeft.wait(500);
				} catch (InterruptedException e) {}
				
				int toPrint = (int)( ((float)nTasks-(float)tasksLeft.n)/(float)nTasks*100-printed );
				for(int i=0; i<toPrint; i++)
					System.out.printf("*");
				printed += toPrint;
			}
		}
		
		System.out.printf("\n");
		long time_ms = timer.timeElapsed();
		long time_s = time_ms / 1000;
		long time_min =  time_s / 60;
		String timing_output = String.format("Image computed in %d ms = %d min, %d sec.\n", time_ms, time_min, time_s - time_min*60);
		System.out.print(timing_output);
		
		// Tone map output image and writ to file
		BufferedImage image = scene.getTonemapper().process(scene.getFilm());
		
		ImageWriter.writePng(image, scene.getOutputFilename());
		try {
			PrintWriter writer = new PrintWriter(scene.getOutputFilename()+".txt", "UTF-8");
			writer.print(timing_output);
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
}
