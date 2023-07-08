package aircraftsimulator.GameObject.Aircraft.Spawner;

import aircraftsimulator.GameObject.Aircraft.Missile;
import aircraftsimulator.GameObject.DestructibleObjectInterface;
import aircraftsimulator.GameObject.GameObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MissileLauncher<T extends  Missile> extends TargetTimerSpawner<T>{

    private Map<DestructibleObjectInterface, List<T>> missileMap;
    private int numMissiles;

    public MissileLauncher(GameObject parent, float interval, int numMissiles) {
        super(parent, interval);
        missileMap = new HashMap<>();
        this.numMissiles = numMissiles;
    }

    public void shareMissileFireInformation()
    {

    }
}
