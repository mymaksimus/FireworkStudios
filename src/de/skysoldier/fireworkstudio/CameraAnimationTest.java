package de.skysoldier.fireworkstudio;

import java.util.ArrayList;
import java.util.Arrays;

import org.lwjgl.util.vector.Vector3f;

import de.skysoldier.abstractgl2.mklmbversion.lib.AGL3dCamera;
import de.skysoldier.abstractgl2.mklmbversion.lib.AGLRenderController;
import de.skysoldier.abstractgl2.mklmbversion.lib.AGLResource;

public class CameraAnimationTest {
	
	private AGL3dCamera camera;
	private ArrayList<CameraAnimationKeyframe> keyFrames;
	private CameraAnimationKeyframe current, next;
	private boolean finished;
	
	public CameraAnimationTest(AGL3dCamera camera, AGLResource animationResource){
		this.camera = camera;
		keyFrames = new ArrayList<>();
		ArrayList<String> rawKeyFrames = animationResource.toLineListResource();
		for(String s : rawKeyFrames){
			s = s.replaceAll("[eyetoup( ]", "");
			s = s.replace(")", ",");
			String data[] = s.split(",");
			System.out.println(s + ", " + Arrays.toString(data));
			Vector3f eye = new Vector3f(Float.valueOf(data[0]), Float.valueOf(data[1]), Float.valueOf(data[2]));
			Vector3f to = new Vector3f(Float.valueOf(data[3]), Float.valueOf(data[4]), Float.valueOf(data[5]));
			Vector3f up = new Vector3f(Float.valueOf(data[6]), Float.valueOf(data[7]), Float.valueOf(data[8]));
			keyFrames.add(new CameraAnimationKeyframe(eye, to, up, Float.valueOf(data[9])));
		}
		if(keyFrames.size() < 2) throw new IllegalArgumentException("sense?? Animation with 1 keyframe or what?");
	}
	
	public void addKeyFrameListener(int keyFrameIndex, Runnable listener){
		keyFrames.get(keyFrameIndex).setListener(listener);
	}
	
	public void init(){
		current = keyFrames.remove(0);
		next = keyFrames.remove(0);
		camera.lookAt(current.getEye(), current.getAt(), current.getUp());
	}
	
	public void update(){
		if(!finished){
			float currentState = (AGLRenderController.getTicksInSeconds() - current.getTime()) / (next.getTime() - current.getTime());
			Vector3f newEye = linearTransitionStep(current.getEye(), next.getEye(), currentState);
			Vector3f newAt = linearTransitionStep(current.getAt(), next.getAt(), currentState);
			Vector3f newUp = linearTransitionStep(current.getUp(), next.getUp(), currentState);
			camera.lookAt(newEye, newAt, newUp);
			if(currentState > 1){
				camera.lookAt(next.getEye(), next.getAt(), next.getUp());
				if(!keyFrames.isEmpty()){
					Runnable r = current.getListener();
					if(r != null) r.run();
					current = next;
					next = keyFrames.remove(0);
				}
				else {
					finished = true;
				}
			}
		}
	}
	
	private Vector3f linearTransitionStep(Vector3f from, Vector3f to, float state){
		float newx = (to.x - from.x) * state + from.x;
		float newy = (to.y - from.y) * state + from.y;
		float newz = (to.z - from.z) * state + from.z;
		return new Vector3f(newx, newy, newz);
	}
	
	static class CameraAnimationKeyframe implements Comparable<CameraAnimationKeyframe> {
		
		private Vector3f eye, at, up;
		private float time;
		private Runnable listener;
		
		public CameraAnimationKeyframe(Vector3f eye, Vector3f at, Vector3f up, float time){
			this.eye = eye;
			this.at = at;
			this.up = up;
			this.time = time;
		}
		
		public void setListener(Runnable listener){
			this.listener = listener;
		}
		
		public Runnable getListener(){
			return listener;
		}
		
		public int compareTo(CameraAnimationKeyframe cakf){
			if(cakf.getTime() > time) return 1;
			else if(cakf.getTime() < time) return -1;
			return 0;
		}
		
		public Vector3f getEye(){
			return eye;
		}

		public Vector3f getAt(){
			return at;
		}

		public Vector3f getUp(){
			return up;
		}

		public float getTime(){
			return time;
		}
	}
}