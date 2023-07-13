package aircraftsimulator.GameObject.Aircraft.Communication.Information;

import aircraftsimulator.GameObject.GameObject;

import javax.vecmath.Vector3f;

public class FireMotionInformation extends MotionInformationImp implements FireInformation{
    public FireMotionInformation(GameObject source, Vector3f position, Vector3f velocity, Vector3f acceleration, Vector3f direction) {
        super(source, position, velocity, acceleration, direction);
    }

    public FireMotionInformation(MotionInformation m) {
        super(m);
    }
}
