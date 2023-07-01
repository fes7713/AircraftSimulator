package aircraftsimulator.GameObject.Aircraft.Spawner;

import aircraftsimulator.Environment;
import aircraftsimulator.GameObject.Aircraft.Spawner.Trigger.AllTrueTrigger;
import aircraftsimulator.GameObject.Aircraft.Spawner.Trigger.TimeTrigger;
import aircraftsimulator.GameObject.Aircraft.Spawner.Trigger.TriggerInterface;
import aircraftsimulator.GameObject.Component.Component;
import aircraftsimulator.GameObject.GameObject;

import java.awt.*;

public abstract class Spawner<T extends GameObject> extends Component implements SpawnerInterface<T> {
    protected GameObject parent;
    protected TriggerInterface trigger;


    public Spawner(GameObject parent)
    {
        this.parent = parent;
        trigger = new AllTrueTrigger();
    }

    @Override
    public void update(float delta) {
        if(trigger.update(delta))
        {
            spawn();
        }
    }

    @Override
    public void draw(Graphics2D g2d) {

    }

    @Override
    public void setParent(GameObject parent) {
        this.parent = parent;
    }

    @Override
    public void spawn() {
        T obj = createObject();
        if(obj != null)
        {
            Environment.getInstance().addObject(obj);
        }
    }

    @Override
    public T createObject() {
        return null;
    }

    @Override
    public void setTrigger(TriggerInterface trigger) {
        this.trigger = trigger;
    }
}
