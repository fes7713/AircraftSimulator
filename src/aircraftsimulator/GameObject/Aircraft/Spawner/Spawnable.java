package aircraftsimulator.GameObject.Aircraft.Spawner;

import javax.vecmath.Vector3f;

public interface Spawnable {
    void activate(Vector3f position, Vector3f velocity, Vector3f direction);
}
