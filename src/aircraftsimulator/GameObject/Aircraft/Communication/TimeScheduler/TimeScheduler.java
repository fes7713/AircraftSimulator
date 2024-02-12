package aircraftsimulator.GameObject.Aircraft.Communication.TimeScheduler;

public interface TimeScheduler {
    boolean update(float delta);
    float getProcessTime();
}
