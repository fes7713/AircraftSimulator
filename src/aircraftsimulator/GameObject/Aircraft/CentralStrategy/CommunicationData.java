package aircraftsimulator.GameObject.Aircraft.CentralStrategy;

import aircraftsimulator.GameObject.Aircraft.Communication.Data.Data;

public class CommunicationData implements Data {
    protected final Data data;
    protected final float frequency;

    public CommunicationData(Data data, float frequency) {
        this.data = data;
        this.frequency = frequency;
    }

    public Data getData() {
        return data;
    }

    public float getFrequency() {
        return frequency;
    }
}
