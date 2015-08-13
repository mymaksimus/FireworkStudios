package de.skysoldier.fireworkstudio;

import org.lwjgl.util.vector.Vector3f;

import de.skysoldier.abstractgl2.mklmbversion.lib.AGL;
import de.skysoldier.abstractgl2.mklmbversion.lib.AGLAsset;
import de.skysoldier.abstractgl2.mklmbversion.lib.AGLRenderController;
import de.skysoldier.abstractgl2.mklmbversion.lib.AGLRenderObject;
import de.skysoldier.abstractgl2.mklmbversion.lib.AGLUniform;
import de.skysoldier.abstractgl2.mklmbversion.lib.AGLUniformBinding;
import de.skysoldier.abstractgl2.mklmbversion.lib.AGLViewPart;

public class Rocket {
	
	public Rocket(Firework firework, FireworkListener listener, AGLAsset topAsset, AGLAsset bodyAsset, Vector3f startPosition, AGLUniform colorUniform, AGLViewPart viewPart){
		AGLUniformBinding b = new AGLUniformBinding(colorUniform);
		b.setData(0, 0, 0, 0);
		TopPart tp = new TopPart(topAsset);
		BodyPart bp = new BodyPart(tp, bodyAsset, firework, listener);
		bp.translateGlobal(startPosition);
		tp.translate(startPosition);
		tp.addUniformBinding(b);
		bp.addUniformBinding(b);
		viewPart.addRenderObjects(tp, bp);
	}
	
	static class TopPart extends AGLRenderObject {
		
		public TopPart(AGLAsset asset){
			super(asset);
		}
	}
	
	static class BodyPart extends AGLRenderObject {
		
		private TopPart topPart;
		private float dx, dz;
		private Firework firework;
		private FireworkListener listener;
		private boolean exploded;
		
		public BodyPart(TopPart topPart, AGLAsset asset, Firework firework, FireworkListener listener){
			super(asset);
			this.topPart = topPart;
			this.listener = listener;
			this.firework = firework;
		}
		
		public void update(){
			if(firework.isLaunched()){
				super.update();
				float lifeTime = (AGL.getTime() - getCreationTime()) / (float) 1e9;
				float deltas = AGLRenderController.getDeltaS();
				float dy = yFunction(lifeTime);
				dx *= deltas;
				dy *= deltas * lifeTime;
				dz *= deltas;
				translateGlobal(dx, dy, dz);
				topPart.translateGlobal(dx, dy, dz);
				if(lifeTime >= 3 && !exploded){
					listener.launchFirework(firework, getPosition());
					exploded = true;
					setRemoveRequested(true);
					topPart.setRemoveRequested(true);
				}
			}
			else {
				fakeCreationTime(AGL.getTime());
			}
		}
		
		private float yFunction(float t){
			return 10f;
		}
	}
}
