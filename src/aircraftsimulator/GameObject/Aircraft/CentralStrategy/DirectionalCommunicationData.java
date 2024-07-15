package aircraftsimulator.GameObject.Aircraft.CentralStrategy;

import aircraftsimulator.GameObject.Aircraft.Communication.Data.Data;

import javax.vecmath.Vector3f;

public class DirectionalCommunicationData extends CommunicationData{
    private final Vector3f target;
    private final Vector3f source;

    public DirectionalCommunicationData(Data data, float frequency, Vector3f target, Vector3f source) {
        super(data, frequency);
        this.target = target;
        this.source = source;
    }

    public CommunicationData communicationData()
    {
        return new CommunicationData(data, frequency);
    }

    public Vector3f getTarget() {
        return target;
    }

    public Vector3f getSource() {
        return source;
    }
}
