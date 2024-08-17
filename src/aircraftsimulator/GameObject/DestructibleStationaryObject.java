package aircraftsimulator.GameObject;

import aircraftsimulator.GameObject.Aircraft.DamageReceiver;

import javax.vecmath.Vector3f;
import java.awt.*;

public class DestructibleStationaryObject extends GameObject implements DestructibleObjectInterface, DamageReceiver {
    private float health;

    public DestructibleStationaryObject(Team team, Vector3f position, Color color, float size, float mass, float health) {
        super(team, position, color, size, mass);
        this.health = health;
    }


    @Override
    public float getHealth() {
        return health;
    }

    public void takeDamage(float damage){
        // TODO
        // Have indirect layer of damage calculation.
        // Consider having armor
        // Decorator pattern for armor
        // Reduce damage first by decorator pattern;
        health -= damage;
        if(!isAlive())
            destroyed();
    }

    @Override
    public void destroyed() {
        // remove from list
        remove();
        health = 0;
    }

    @Override
    public boolean isAlive() {
        return health > 0;
    }
}
