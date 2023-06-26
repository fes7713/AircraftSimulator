package aircraftsimulator.GameObject.Aircraft;

import aircraftsimulator.GameObject.GameObjectInterface;

import javax.vecmath.Vector3f;
import java.util.List;

public interface MovingObjectInterface extends GameObjectInterface {
    Vector3f getDirection();
    Vector3f getAcceleration();
    Vector3f getVelocity();
    List<ForceApplier> getForceList();
}
