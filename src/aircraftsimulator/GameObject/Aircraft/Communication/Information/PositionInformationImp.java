package aircraftsimulator.GameObject.Aircraft.Communication.Information;

import aircraftsimulator.GameObject.GameObject;

import javax.vecmath.Vector3f;

public class PositionInformationImp implements PositionInformation{
    private final GameObject source;
    private final Vector3f position;

    public PositionInformationImp(GameObject source, Vector3f position){
        this.source = source;
        this.position = new Vector3f(position);
    }

    // Send copy
    public Vector3f getPosition() {
        return new Vector3f(position);
    }

    @Override
    public GameObject getSource() {
        return source;
    }
}
