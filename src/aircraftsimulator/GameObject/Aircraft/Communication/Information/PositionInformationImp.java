package aircraftsimulator.GameObject.Aircraft.Communication.Information;

import aircraftsimulator.GameObject.GameObjectInterface;

import javax.vecmath.Vector3f;

public class PositionInformationImp implements PositionInformation{
    private final GameObjectInterface source;
    private final Vector3f position;

    public PositionInformationImp(PositionInformation positionInformation){
        this.source = positionInformation.getSource();
        this.position = new Vector3f(positionInformation.getPosition());
    }

    public PositionInformationImp(GameObjectInterface source, Vector3f position){
        this.source = source;
        this.position = new Vector3f(position);
    }

    // Send copy
    public Vector3f getPosition() {
        return new Vector3f(position);
    }

    @Override
    public Vector3f getDirection() {
        return null;
    }

    @Override
    public GameObjectInterface getSource() {
        return source;
    }
}
