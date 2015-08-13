package de.skysoldier.fireworkstudio;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.JFileChooser;

import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import de.skysoldier.abstractgl2.mklmbversion.lib.AGL3dApplication;
import de.skysoldier.abstractgl2.mklmbversion.lib.AGL3dCamera;
import de.skysoldier.abstractgl2.mklmbversion.lib.AGLAsset;
import de.skysoldier.abstractgl2.mklmbversion.lib.AGLCaps.AGLDisplayCap;
import de.skysoldier.abstractgl2.mklmbversion.lib.AGLCaps.AGLDrawMode;
import de.skysoldier.abstractgl2.mklmbversion.lib.AGLDisplay;
import de.skysoldier.abstractgl2.mklmbversion.lib.AGLGlslAttribute;
import de.skysoldier.abstractgl2.mklmbversion.lib.AGLMesh;
import de.skysoldier.abstractgl2.mklmbversion.lib.AGLMeshData;
import de.skysoldier.abstractgl2.mklmbversion.lib.AGLModelLoader;
import de.skysoldier.abstractgl2.mklmbversion.lib.AGLProjection;
import de.skysoldier.abstractgl2.mklmbversion.lib.AGLRenderController;
import de.skysoldier.abstractgl2.mklmbversion.lib.AGLResource;
import de.skysoldier.abstractgl2.mklmbversion.lib.AGLShaderProgram;
import de.skysoldier.abstractgl2.mklmbversion.lib.AGLTexture;
import de.skysoldier.abstractgl2.mklmbversion.lib.AGLUniform;
import de.skysoldier.abstractgl2.mklmbversion.lib.AGLUniformType;
import de.skysoldier.abstractgl2.mklmbversion.lib.AGLView;
import de.skysoldier.abstractgl2.mklmbversion.lib.AGLViewPart;

public class FireworkApplication extends AGL3dApplication implements FireworkListener {

	private AGLViewPart fireworkViewPart;
	private AGLAsset rocketTopAsset, rocketBodyAsset, particleAsset;
	private AGLUniform colorUniform;
	private LinkedBlockingQueue<Particle> particlesToEmit = new LinkedBlockingQueue<>();
	private LinkedBlockingQueue<Firework> fireworks = new LinkedBlockingQueue<>();
	private LinkedBlockingQueue<Firework> fireworksToExplode = new LinkedBlockingQueue<>();
	private float step = 200, state;
	private boolean running;
	private CameraAnimationTest cameraAnimationTest;
	
	public FireworkApplication(){
		build();
	}
	
	public void build(){
		AGLGlslAttribute attributes[] = new AGLGlslAttribute[]{
			AGLGlslAttribute.createAttributeVec3("vertexIn"),
			AGLGlslAttribute.createAttributeVec2("texCoords")
		};
		AGLShaderProgram fireworkShader = new AGLShaderProgram(new AGLResource("firework.shader"), "###", attributes);
		AGLView world = new AGLView(getCamera());
		fireworkViewPart = new AGLViewPart(fireworkShader);
		world.addViewPart(fireworkViewPart);
		float particleData[] = AGLModelLoader.loadObj(new AGLResource("particle.obj"), 0.25f)[0];
		float rocketData[][] = AGLModelLoader.loadObj(new AGLResource("rocket.obj"), 0.03f);
		AGLTexture particleTexture = new AGLTexture(new AGLResource("particle.png"));
		AGLTexture rocketTopTexture = new AGLTexture(new AGLResource("rockettop.png"));
		AGLTexture rocketBodyTexture = new AGLTexture(new AGLResource("rocketbody.png"));
		particleAsset = createAsset(particleData, AGLDrawMode.TRIANGLES, attributes, particleTexture);
		rocketTopAsset = createAsset(rocketData[0], AGLDrawMode.TRIANGLES, attributes, rocketTopTexture);
		rocketBodyAsset = createAsset(rocketData[1], AGLDrawMode.TRIANGLES, attributes, rocketBodyTexture);
		colorUniform = new AGLUniform(AGLUniformType.VEC4, fireworkShader, "color");
		AGLRenderController.init(true, true);
		AGLRenderController.bindViews(world);
//		getCamera().translateToGlobal(0, -10, -100);
//		getCamera().lookAt(new Vector3f(0, 0, 200), new Vector3f(0, -30, 0), new Vector3f(0, 1, 0));
		cameraAnimationTest = new CameraAnimationTest(getCamera(), new AGLResource("test.camanim"));
		cameraAnimationTest.addKeyFrameListener(0, new Runnable(){
			public void run(){
				running = true;
			}
		});
		cameraAnimationTest.init();
		super.build();
	}
	
	public AGLAsset createAsset(float[] data, AGLDrawMode drawMode, AGLGlslAttribute attributes[], AGLTexture texture){
		return new AGLAsset(new AGLMesh(new AGLMeshData(data, drawMode), attributes), texture);
	}
	
	public void loadFireworksFromImage(AGLResource resource, float scaling){
		BufferedImage image = resource.toImageResource();
		//black = no firework, color = firework with this color
		for(int i = 0; i < image.getWidth(); i++){
			for(int j = 0; j < image.getHeight(); j++){
				float x = i, z = j;
				Color rgbColor = new Color(image.getRGB(i, j));
				if(!rgbColor.equals(Color.BLACK)){
					float y = -30.0f;
					float xs = x * scaling, zs = z * scaling;
					float xsh = x * scaling + 0.5f * scaling, zsh = z * scaling + 0.5f * scaling;
					createFirework(xs, y, zs, rgbColor);
					createFirework(xsh, y, zs, rgbColor);
					createFirework(xs, y, zsh, rgbColor);
					createFirework(xsh, y, zsh, rgbColor);
				}
			}
		}
	}
	
	public void createFirework(float x, float y, float z, Color rgbColor){
		Vector3f position = new Vector3f(x, y, z);
		Vector4f color = new Vector4f(rgbColor.getRed() / 255.0f, rgbColor.getGreen() / 255.0f, rgbColor.getBlue() / 255.0f, 1.0f);
		Firework f = new Firework(this, rocketTopAsset, rocketBodyAsset, particleAsset, position, color, colorUniform, fireworkViewPart);
		fireworks.add(f);
		f.create();
	}
	
	public void run(){
		super.run();
		while(!particlesToEmit.isEmpty()){
			fireworkViewPart.addRenderObjects(particlesToEmit.poll());
		}
		while(!fireworksToExplode.isEmpty()){
			fireworksToExplode.poll().explode();
		}
		state += AGLRenderController.getDeltaMs();
//		if(Keyboard.isKeyDown(Keyboard.KEY_T)) running = true;
		if(running){
			if(state > step){
				if(!fireworks.isEmpty()){
					fireworks.poll().launche();
					state = 0;
				}
			}
		}
		cameraAnimationTest.update();
	}
	
	public AGLDisplay buildDisplay(){
		return new AGLDisplay(AGLDisplayCap.FULLSCREEN);
	}
	
	public AGL3dCamera buildCamera(){
		return new AGL3dCamera(new AGLProjection.PerspectiveProjection(45.0f, 0.1f, 100.0f));
	}
	
	public void emitParticle(Particle particle){
		particlesToEmit.add(particle);
	}
	
	public void launchFirework(Firework firework, Vector3f position){
		fireworksToExplode.add(firework);
		firework.setPosition(position);
	}
	
	public static void main(String[] args){
		JFileChooser chooser = new JFileChooser();
		chooser.showOpenDialog(null);
//		s.loadFireworksFromImage(new AGLResource("fireworks/firework3.png"), 5.0f);
		FireworkApplication s = new FireworkApplication();
		s.loadFireworksFromImage(new AGLResource(chooser.getSelectedFile()), 5.0f);
		s.runGameLoop(10);
	}
}