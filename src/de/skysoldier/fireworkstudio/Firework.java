package de.skysoldier.fireworkstudio;

import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import de.skysoldier.abstractgl2.mklmbversion.lib.AGL;
import de.skysoldier.abstractgl2.mklmbversion.lib.AGLAsset;
import de.skysoldier.abstractgl2.mklmbversion.lib.AGLUniform;
import de.skysoldier.abstractgl2.mklmbversion.lib.AGLViewPart;

public class Firework {
	
	private FireworkApplication show;
	private AGLAsset particleAsset, rocketTopAsset, rocketBodyAsset;
	private Vector3f position;
	private Vector4f color;
	private AGLUniform colorUniform;
	private AGLViewPart fireworkViewPart;
	private boolean launched;
	
	public Firework(FireworkApplication show, AGLAsset rocketTopAsset, AGLAsset rocketBodyAsset, AGLAsset fireworkParticleAsset, Vector3f position, Vector4f color, AGLUniform colorUniform, AGLViewPart fireworkViewPart){
		this.show = show;
		this.rocketTopAsset = rocketTopAsset;
		this.rocketBodyAsset = rocketBodyAsset;
		this.particleAsset = fireworkParticleAsset;
		this.position = position;
		this.color = color;
		this.colorUniform = colorUniform;
		this.fireworkViewPart = fireworkViewPart;
	}
	
	public void setPosition(Vector3f position){
		this.position = position;
	}
	
	public void create(){
		new Rocket(this, show, rocketTopAsset, rocketBodyAsset, position, colorUniform, fireworkViewPart);
	}
	
	public void launche(){
		launched = true;
	}
	
	public boolean isLaunched(){
		return launched;
	}
	
	public void explode(){
		for(int i = 0; i < 100; i++){
			Particle p = new Particle(particleAsset, position, color, colorUniform);
			p.setParticleListener(show);
			p.setFireworkCreationTime(AGL.getTime());
			fireworkViewPart.addRenderObjects(p);
		}
	}
}
