package aircraftsimulator.GameObject;

import aircraftsimulator.Environment;
import aircraftsimulator.GameObject.Aircraft.DamageReceiver;
import aircraftsimulator.GamePanel;

import javax.vecmath.Vector3f;
import java.awt.*;

public class DestructibleObject extends GameObject implements DestructibleObjectInterface, DamageReceiver {
    private float health;

    public DestructibleObject(Vector3f position, Color color, float size, float health)
    {
        super(position, color, size);
        this.health = health;
    }

    @Override
    public float getHealth() {
        return 0;
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
        Environment.getInstance().removeObject(this);
    }

    @Override
    public boolean isAlive() {
        return health > 0;
    }
}
