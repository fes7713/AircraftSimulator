package aircraftsimulator.GameObject.Aircraft.Spawner;

import aircraftsimulator.GameObject.Aircraft.Missile;
import aircraftsimulator.GameObject.Aircraft.MovingObjectInterface;
import aircraftsimulator.GameObject.DestructibleObjectInterface;
import aircraftsimulator.GameObject.GameObject;

import javax.vecmath.Vector3f;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MissileLauncher extends TargetTimerSpawner<Missile> implements WeaponSystem{

    private Map<DestructibleObjectInterface, List<Missile>> missileMap;
    private int numMissiles;

    private final Missile sample;

    public MissileLauncher(GameObject parent, float interval, int numMissiles) {
        super(parent, interval);
        missileMap = new HashMap<>();
        this.numMissiles = numMissiles;

        sample = new Missile(parent.getTeam(), 10, 120);
    }

    public void shareMissileFireInformation()
    {

    }

    @Override
    public Missile createObject() {
        if(target == null)
            return null;
        if(numMissiles <= 0)
            return null;

        Missile clone = sample.clone();
        if(parent instanceof MovingObjectInterface m)
            clone.activate(parent.getPosition(), m.getVelocity(), m.getDirection());
        else
        {
            Vector3f direction= new Vector3f(target.getPosition());
            direction.sub(parent.getPosition());
            clone.activate(parent.getPosition(), new Vector3f(), direction);
        }
        numMissiles--;

        return clone;
    }

    @Override
    public float getRange() {
        if(parent instanceof MovingObjectInterface m)
            sample.getVelocity().set(m.getVelocity().length(), 0, 0);
        else
            sample.getVelocity().set(0, 0, 0);
        return sample.getRange();
    }
}
