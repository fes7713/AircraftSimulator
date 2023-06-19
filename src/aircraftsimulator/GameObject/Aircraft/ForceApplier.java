package aircraftsimulator.GameObject.Aircraft;

import javax.vecmath.Vector3f;

public interface ForceApplier {
    Vector3f generateForce(Vector3f direction);
}
