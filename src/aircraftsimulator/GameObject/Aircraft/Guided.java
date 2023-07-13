package aircraftsimulator.GameObject.Aircraft;

import aircraftsimulator.GameObject.Aircraft.Communication.ReceiverInterface;

public interface Guided extends ReceiverInterface {
    default boolean isActive(){
        return true;
    };
}
