package aircraftsimulator.GameObject.Aircraft;

import aircraftsimulator.Animation.AnimationManager;
import aircraftsimulator.Animation.TextAnimation.TextAnimation;
import aircraftsimulator.GameObject.Aircraft.Fuse.ContactFuse;
import aircraftsimulator.GameObject.Aircraft.Fuse.FuseInterface;
import aircraftsimulator.GameObject.DestructibleObjectInterface;
import aircraftsimulator.GameObject.Team;

import javax.vecmath.Vector3f;
import java.awt.*;

public class Bullet extends MovingObject implements DamageGenerator {

    // For calculation purpose
    private final DestructibleObjectInterface target;
    private final FuseInterface fuse;
    private final float baseDamage;
//
//    private final float flightTime;
//    private final float distanceTraveled;

    public final static float BULLET_SIZE = 2;
    public final static Color BULLET_COLOR = Color.ORANGE;
    public final static float MINIMUM_SPEED = 25;
    public final static float BULLET_AIR_RESISTANCE = 0.01F;

    public Bullet(Team team, DestructibleObjectInterface target, Vector3f position, Vector3f velocity, Color color, float baseDamage) {
        super(team, position, velocity, color, BULLET_SIZE, BULLET_AIR_RESISTANCE);
        this.target = target;
        this.baseDamage = baseDamage;
        fuse = new ContactFuse(this);
        minimumSpeed = MINIMUM_SPEED;
    }

    public Bullet(Team team, DestructibleObjectInterface target, Vector3f position, Vector3f velocity, float baseDamage) {
        this(team, target, position, velocity, BULLET_COLOR, baseDamage);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        if(fuse.trigger(target))
        {
            target.takeDamage(getDamage());
            remove();
            AnimationManager.Add(TextAnimation.make("Hit", position, color));
        }

        // Less than minimum speed
        if(velocity.lengthSquared() < minimumSpeed * minimumSpeed)
        {
            remove();
            AnimationManager.Add(TextAnimation.make("Missed", position, color));

        }
    }

    @Override
    public float getDamage() {
        return baseDamage;
    }

    public float getFlightTime()
    {
        return (1 / minimumSpeed - 1 / velocity.length()) / airResistance.getCoefficient();
    }
}
