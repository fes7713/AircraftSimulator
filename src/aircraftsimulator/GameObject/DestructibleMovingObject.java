package aircraftsimulator.GameObject;

import aircraftsimulator.GameObject.Aircraft.DamageReceiver;
import aircraftsimulator.GameObject.Aircraft.MovingObject;

import javax.vecmath.Vector3f;
import java.awt.*;

public class DestructibleMovingObject extends MovingObject implements DestructibleObjectInterface, DamageReceiver {
    private float health;

    protected DestructibleMovingObject(DestructibleMovingObject d) {
        super(d);
        health = d.health;
    }

    public DestructibleMovingObject(Team team, Vector3f position, Vector3f velocity, Color color, float size, float health) {
        super(team, position, velocity, color, size);
        this.health = health;
    }

    public DestructibleMovingObject(Team team, Vector3f position, Vector3f velocity, Color color, float size, float airResistanceCoefficient, float health) {
        super(team, position, velocity, color, size, airResistanceCoefficient);
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
