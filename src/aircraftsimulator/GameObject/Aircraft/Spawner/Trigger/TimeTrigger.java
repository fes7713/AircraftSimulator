package aircraftsimulator.GameObject.Aircraft.Spawner.Trigger;

public class TimeTrigger implements TriggerInterface, Cloneable{
    private float interval;
    private final float intervalStart;

    public TimeTrigger(float interval) {
        this.interval = interval;
        this.intervalStart = interval;
    }

    @Override
    public boolean update(float delta) {
        interval -= delta;
        if(interval < 0)
        {
            interval = intervalStart;
            return true;
        }
        return false;
    }

    @Override
    public boolean isAvailable() {
        return interval < 0;
    }

    @Override
    public TimeTrigger clone() {
        try {
            return (TimeTrigger) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
