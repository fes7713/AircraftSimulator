package aircraftsimulator.GameObject.Aircraft.Spawner.Trigger;

public interface TriggerInterface extends Cloneable{
    boolean update(float delta);
    boolean isAvailable();
    TriggerInterface clone();
}
