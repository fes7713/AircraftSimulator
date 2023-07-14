package aircraftsimulator.GameObject.Aircraft.Spawner;

import aircraftsimulator.GameObject.Aircraft.Communication.Information.FireInformation;
import aircraftsimulator.GameObject.Aircraft.GuideNetwork;
import aircraftsimulator.GameObject.Aircraft.Guided;
import aircraftsimulator.GameObject.Aircraft.Missile;
import aircraftsimulator.GameObject.Aircraft.MovingObjectInterface;
import aircraftsimulator.GameObject.DestructibleObjectInterface;
import aircraftsimulator.GameObject.GameObject;

import javax.vecmath.Vector3f;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MissileLauncher extends TargetTimerSpawner<Missile> implements LongRangeWeaponSystem{

    private final Map<DestructibleObjectInterface, List<Missile>> missileMap;
    private int numMissiles;
    private int missilePerTarget;

    private final Missile sample;

    private static final float MISSILE_HEALTH = 10;
    private static final int MISSILE_PER_TARGET = 2;

    public MissileLauncher(GameObject parent, float interval, int numMissiles, float damage) {
        this(parent, new Missile(parent.getTeam(), MISSILE_HEALTH, damage), interval, numMissiles);
    }

    public MissileLauncher(GameObject parent, Missile sample, float interval, int numMissiles) {
        this(parent, sample, new HashMap<>(), interval, numMissiles);
    }

    public MissileLauncher(GameObject parent, Map<DestructibleObjectInterface, List<Missile>> missileMap, float interval, int numMissiles, float damage) {
        this(parent, new Missile(parent.getTeam(), MISSILE_HEALTH, damage), missileMap, interval, numMissiles);
    }

    public MissileLauncher(GameObject parent, Missile sample, Map<DestructibleObjectInterface, List<Missile>> missileMap, float interval, int numMissiles) {
        super(parent, interval);
        this.missileMap = missileMap;
        this.numMissiles = numMissiles;
        this.sample = sample;
        missilePerTarget = MISSILE_PER_TARGET;
    }

    public Map<DestructibleObjectInterface, List<Missile>> getMissileMap()
    {
        return missileMap;
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
        if(parent instanceof GuideNetwork n && clone instanceof Guided g)
            n.connectToGuidance(g, target);
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

    @Override
    public void fire(FireInformation fireInformation) {
        receive(fireInformation);
    }

    @Override
    public boolean isAvailable() {
        return numMissiles > 0;
    }
}
