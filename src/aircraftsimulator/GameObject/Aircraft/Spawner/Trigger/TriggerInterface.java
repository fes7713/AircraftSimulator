package aircraftsimulator.GameObject.Aircraft.Spawner.Trigger;

public interface TriggerInterface {
    boolean update(float delta);
    boolean isAvailable();
}
