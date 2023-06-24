package aircraftsimulator.GameObject.Aircraft.Communication.Information;

import javax.vecmath.Vector3f;

public class PositionInformation extends Information{
    private final Vector3f position;

    public PositionInformation(Vector3f position){
        this.position = new Vector3f(position);
    }

    // Send copy
    public Vector3f getPosition() {
        return new Vector3f(position);
    }
}
