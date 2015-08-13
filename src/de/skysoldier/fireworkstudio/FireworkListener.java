package de.skysoldier.fireworkstudio;

import org.lwjgl.util.vector.Vector3f;


public interface FireworkListener {
	void emitParticle(Particle particle);
	void launchFirework(Firework firework, Vector3f position);
}
