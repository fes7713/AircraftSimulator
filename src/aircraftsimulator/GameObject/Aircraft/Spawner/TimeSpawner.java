package aircraftsimulator.GameObject.Aircraft.Spawner;

import aircraftsimulator.GameObject.Aircraft.Spawner.Trigger.TimeTrigger;
import aircraftsimulator.GameObject.GameObject;

public class TimeSpawner<T extends GameObject> extends Spawner<T>{
    public TimeSpawner(GameObject parent, float interval) {
        super(parent);
        trigger = new TimeTrigger(interval);
    }
}
