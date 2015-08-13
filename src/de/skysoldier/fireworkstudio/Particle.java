package de.skysoldier.fireworkstudio;

import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import de.skysoldier.abstractgl2.mklmbversion.lib.AGL;
import de.skysoldier.abstractgl2.mklmbversion.lib.AGLAsset;
import de.skysoldier.abstractgl2.mklmbversion.lib.AGLRenderController;
import de.skysoldier.abstractgl2.mklmbversion.lib.AGLRenderObject;
import de.skysoldier.abstractgl2.mklmbversion.lib.AGLUniform;
import de.skysoldier.abstractgl2.mklmbversion.lib.AGLUniformBinding;

public class Particle extends AGLRenderObject {

	private float beta;
	private float dx, dy, dz, speed, intensity;
	private AGLUniformBinding binding;
	private FireworkListener listener;
	private int emitLayer;
	private float fireworkCreationTime;
	private Vector3f startPosition;
	private Vector4f color = new Vector4f(0, 0, 0, 0);
	private boolean emitted = false;
	
	private static final int EMIT_LAYERS = 20;
	
	public Particle(AGLAsset asset, Vector4f color, int emitLayer){
		super(asset);
		this.color = color;
		this.emitLayer = emitLayer;
	}
		
	public Particle(AGLAsset asset, Vector3f startPosition, Vector4f color, AGLUniform uniform){
		this(asset, uniform, startPosition, color, AGL.getRandom().nextFloat() * 180, AGL.getRandom().nextFloat() * 360 - 180);
	}
	
	public Particle(AGLAsset asset, AGLUniform uniform, Vector3f startPosition, Vector4f color, float alpha, float beta){
		super(asset);
		emitLayer = Particle.EMIT_LAYERS;
		this.binding = new AGLUniformBinding(uniform); 
		binding.setData(AGL.getVectorData(color));
		this.startPosition = startPosition;
		this.color = color;
		setColorUniformBinding(binding);
		setColorAlphaValue(1);
		float alphaRadians = (float) Math.toRadians(alpha);
		float betaRadians = (float) Math.toRadians(beta);
		dx = (float) (Math.sin(alphaRadians) * Math.cos(betaRadians));
		dy = (float) (Math.sin(alphaRadians) * Math.sin(betaRadians));
		dz = (float) Math.cos(alphaRadians);
		translateGlobal(dx + startPosition.x, dy + startPosition.y, dz + startPosition.z);
		if(alpha == 90) dz = 0;
		this.speed = 30f;
		this.beta = betaRadians;
		if(beta != 90.0f && beta != -90.0f){
			this.intensity = (float) Math.abs(Math.tan(beta));
		}
		if(intensity > 20) intensity = 20;
	}
	
	public void setStartPosition(Vector3f position){
		this.startPosition = position;
	}
	
	public void setIntensity(float intensity){
		this.intensity = intensity;
	}
	
	public void setSpeed(float speed){
		this.speed = speed;
	}
	
	public void setFireworkCreationTime(float time){
		this.fireworkCreationTime = time;
	}
	
	public void setColorUniformBinding(AGLUniformBinding binding){
		this.binding = binding;
		binding.setData(AGL.getVectorData(color));
		addUniformBinding(binding);
	}
	
	public void nextPosition(float lifeTime){
		float deltas = AGLRenderController.getDeltaS();
//		Vector3f step = new Vector3f();
//		step.setX(dx * deltas);
//		step.setY(dy * deltas);
//		step.setZ(dz * deltas);
//		translateGlobal(step);
		
		float ndy = dy * speed;
		ndy += yFunction(lifeTime);
		
		Vector3f position = getPosition();
		translateToGlobal(position.x + dx * speed * deltas, position.y + ndy * deltas, position.z + dz * speed * deltas);
	}
	
	public float yFunction(float t){
//		return (float) (9.81 + Math.abs(Math.tan(beta))) * -2 * t + dy;
		return (float) -9.81 * t + intensity;//(-1.81 * t + (Math.tan((beta))));
	}
	
	public void update(){
		float lifeTimeInSeconds = (float) ((AGL.getTime() - getCreationTime()) / 1e9);
		float fireworkLifeTimeInSeconds = (float) ((AGL.getTime() - fireworkCreationTime) / 1e9);
//		if(fireworkLifeTimeInSeconds > 1) return;
		super.update();
		this.speed *= 0.9999f;
		
		nextPosition(lifeTimeInSeconds);
		
		float alpha = ((emitLayer + 1) / (float) EMIT_LAYERS) - 0.5f * fireworkLifeTimeInSeconds;
		setColorAlphaValue(alpha);
		if(emitLayer == 20){
			binding.setData(1.0f, 1.0f, 1.0f, alpha);
		}
		else {
			binding.setData(AGL.getVectorData(color));
		}
		
		if(emitLayer > 0){
			if(!emitted){
				if(lifeTimeInSeconds > 0.05f){
					Particle next = new Particle(getAsset(), new Vector4f(color), emitLayer - 1);
					next.setSpeed(dx, dy, dz);
					next.setColorUniformBinding(new AGLUniformBinding(binding.getUniform()));
//					float alphaValue = ((float) emitLayer) / 10.0f;
					next.setColorAlphaValue((emitLayer) / (float) EMIT_LAYERS);
					next.setParticleListener(listener);
					next.setBeta(beta);
					next.setFireworkCreationTime(fireworkCreationTime);
//					next.translateToGlobal(getPosition().x - dx / 25.0f, getPosition().y - dy / 25.0f, getPosition().z - dz / 25.0f);
					next.setStartPosition(startPosition);
					next.setSpeed(speed);
					next.translateToGlobal(dx + startPosition.x, dy + startPosition.y, dz + startPosition.z);
					next.setIntensity(intensity);
					listener.emitParticle(next);
					emitted = true;
				}
			}
			else {
//				child.translateToGlobal(getPosition().x - dx, getPosition().y - dy, getPosition().z - dz);
//				child.update();
			}
		}
		if(color.w <= 0) setRemoveRequested(true);
	}
	
	public void setSpeed(float dx, float dy, float dz){
		this.dx = dx;
		this.dy = dy;
		this.dz = dz;
	}
	
	public void setBeta(float beta){
		this.beta = beta;
	}
	
	public void setParticleListener(FireworkListener listener){
		this.listener = listener;
	}
	
	public void setColorAlphaValue(float alpha){
		color.w = alpha;
		binding.setData(AGL.getVectorData(color));
	}
}