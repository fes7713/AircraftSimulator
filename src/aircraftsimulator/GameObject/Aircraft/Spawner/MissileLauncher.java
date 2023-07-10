package aircraftsimulator.GameObject.Aircraft.Spawner;

import aircraftsimulator.GameObject.Aircraft.Aircraft;
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

    private static final float MISSILE_HEALTH = 10;

    public MissileLauncher(GameObject parent, float interval, int numMissiles, float damage) {
        super(parent, interval);
        missileMap = new HashMap<>();
        this.numMissiles = numMissiles;

        sample = new Missile(parent.getTeam(), MISSILE_HEALTH, damage);
    }

    public MissileLauncher(GameObject parent, Missile sample, float interval, int numMissiles) {
        super(parent, interval);
        missileMap = new HashMap<>();
        this.numMissiles = numMissiles;

        this.sample = sample;
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

        Missile clone;
        if(parent instanceof MovingObjectInterface m)
        {
            if(sample.getMinimumSpeed() * sample.getMinimumSpeed() > m.getVelocity().lengthSquared())
                return null;
            clone = sample.clone();
            clone.activate(parent.getPosition(), m.getVelocity(), m.getDirection());
        }
        else
        {
            clone = sample.clone();
            Vector3f direction= new Vector3f(target.getPosition());
            direction.sub(parent.getPosition());
            direction.scale(clone.getMinimumSpeed());
            clone.activate(parent.getPosition(), direction, direction);
        }
        numMissiles--;

        // TODo may need to remove this line
        // No need to share the enemy info?
        if(parent instanceof Aircraft a)
            a.addToNetwork(clone);
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
