package aircraftsimulator.GameObject.Aircraft;

import aircraftsimulator.GameObject.Aircraft.Communication.ReceiverInterface;

import javax.vecmath.Vector3f;

public interface Guided extends ReceiverInterface {
    default boolean isActive(){
        return true;
    };
    Vector3f getPosition();
}
