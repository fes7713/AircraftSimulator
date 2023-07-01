package aircraftsimulator.GameObject.Aircraft.Spawner;

import aircraftsimulator.GameObject.Aircraft.Spawner.Trigger.TriggerInterface;
import aircraftsimulator.GameObject.GameObject;

public interface  SpawnerInterface <T extends GameObject>{
    void spawn();
    T createObject();
    void setTrigger(TriggerInterface trigger);
}
