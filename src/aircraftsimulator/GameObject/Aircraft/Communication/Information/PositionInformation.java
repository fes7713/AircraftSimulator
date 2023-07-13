package aircraftsimulator.GameObject.Aircraft.Communication.Information;

import aircraftsimulator.GameObject.GameObject;

import javax.vecmath.Vector3f;

public interface PositionInformation extends Information{
    Vector3f getPosition();
    @Override
    GameObject getSource();
}
