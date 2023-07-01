package aircraftsimulator.GameObject.Aircraft;

import aircraftsimulator.GameObject.Aircraft.Fuse.ContactFuse;
import aircraftsimulator.GameObject.Aircraft.Fuse.FuseInterface;
import aircraftsimulator.GameObject.DestructibleObjectInterface;

import javax.vecmath.Vector3f;
import java.awt.*;

public class Bullet extends MovingObject implements DamageGenerator {

    // For calculation purpose
    private final DestructibleObjectInterface target;
    private final FuseInterface fuse;
    private final float baseDamage;

    public final static float BULLET_HEALTH = 1;
    public final static float BULLET_SIZE = 2;
    public final static Color BULLET_COLOR = Color.ORANGE;
    public final static float MINIMUM_SPEED = 1;

    public Bullet(DestructibleObjectInterface target, Vector3f position, Vector3f velocity, Color color, float baseDamage) {
        super(position, velocity, color, BULLET_SIZE, BULLET_HEALTH);
        this.target = target;
        this.baseDamage = baseDamage;
        fuse = new ContactFuse(this);
    }

    public Bullet(DestructibleObjectInterface target, Vector3f position, Vector3f velocity, float baseDamage) {
        this(target, position, velocity, BULLET_COLOR, baseDamage);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        if(fuse.trigger(target))
        {
            target.takeDamage(getDamage());
            destroyed();
        }

        // Less than minimum speed
        if(velocity.lengthSquared() < MINIMUM_SPEED * MINIMUM_SPEED)
        {
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
