package aircraftsimulator.GameObject.Aircraft;

import aircraftsimulator.GameObject.Aircraft.Fuse.ContactFuse;
import aircraftsimulator.GameObject.Aircraft.Fuse.FuseInterface;
import aircraftsimulator.GameObject.DestructibleObjectInterface;

import javax.vecmath.Vector3f;
import java.awt.*;

public class Bullet extends  MovingObject implements DamageGenerator {

    // For calculation purpose
    private final DestructibleObjectInterface target;
    private final FuseInterface fuse;
    private final float baseDamage;

    public Bullet(DestructibleObjectInterface target, Vector3f position, Vector3f velocity, Color color, float size, float health, float baseDamage) {
        super(position, velocity, color, size, health);
        this.target = target;
        this.baseDamage = baseDamage;
        fuse = new ContactFuse(this);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        if(fuse.trigger(target))
        {
            target.takeDamage(getDamage());
            destroyed();
        }
    }

    @Override
    public float getDamage() {
        return baseDamage;
    }

    @Override
    public DestructibleObjectInterface getTarget() {
        return target;
    }
}
