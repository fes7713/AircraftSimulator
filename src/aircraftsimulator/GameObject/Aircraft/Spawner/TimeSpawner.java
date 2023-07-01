package aircraftsimulator.GameObject.Aircraft.Spawner;

import aircraftsimulator.Environment;
import aircraftsimulator.GameObject.Aircraft.Spawner.Trigger.TimeTrigger;
import aircraftsimulator.GameObject.Component.Component;
import aircraftsimulator.GameObject.GameObject;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

public class TimeSpawner<T extends GameObject> extends Spawner<T>{
    public TimeSpawner(GameObject parent, float interval) {
        super(parent);
        trigger = new TimeTrigger(interval);
    }
}
