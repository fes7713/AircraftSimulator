package aircraftsimulator.GameObject.Aircraft;

import javax.vecmath.Vector3f;

public interface ForceApplier {
    Vector3f applyForce(Vector3f direction);
}
