package aircraftsimulator.GameObject.Aircraft;

import aircraftsimulator.GameObject.DestructibleObjectInterface;

public interface DamageGenerator {
    float getDamage();
    // Keep target object for the faster calculation
    DestructibleObjectInterface getTarget();
}
