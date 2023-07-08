package aircraftsimulator.GameObject.Aircraft.Spawner.Trigger;

public class AllTrueTrigger implements TriggerInterface{
    @Override
    public boolean update(float delta) {
        return true;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}
