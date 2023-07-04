package aircraftsimulator.GameObject.Aircraft;

import aircraftsimulator.Animation.AnimationManager;
import aircraftsimulator.Animation.TextAnimation.TextAnimation;
import aircraftsimulator.GameObject.Aircraft.Communication.Information.Information;
import aircraftsimulator.GameObject.Aircraft.FlightController.SwitchValueFlightController;
import aircraftsimulator.GameObject.Aircraft.Fuse.ContactFuse;
import aircraftsimulator.GameObject.Aircraft.Fuse.FuseInterface;
import aircraftsimulator.GameObject.DestructibleObjectInterface;
import aircraftsimulator.GameObject.Team;

import javax.vecmath.Vector3f;
import java.awt.*;

public class Missile extends Aircraft implements DamageGenerator{
    private final FuseInterface fuse;
    private final float baseDamage;
    private final float minimumSpeed;

    public final static float MISSILE_SIZE = 4;
    public final static Color MISSILE_COLOR = Color.GRAY;
    public final static float MISSILE_THRUST = 4;
    public final static float MINIMUM_SPEED = 8;
    public final static float MISSILE_AIR_RESISTANCE = 0.015F;

    public Missile(Team team, float health, float baseDamage) {
        this(team, null, new Vector3f(), new Vector3f(), MISSILE_COLOR, MISSILE_SIZE, health, MISSILE_THRUST, baseDamage);
    }

    public Missile(Team team, Information target, Vector3f position, Vector3f velocity, float health, float baseDamage) {
        this(team, target, position, velocity, MISSILE_COLOR, MISSILE_SIZE, health, MISSILE_THRUST, baseDamage);
    }

    public Missile(Team team, Information target, Vector3f position, Vector3f velocity, Color color, float size, float health, float thrusterMagnitude, float baseDamage) {
        super(team, new SwitchValueFlightController<>(), position, velocity, color, size, health, thrusterMagnitude);
        flightControl.setParent(this);
        this.baseDamage = baseDamage;
        fuse = new ContactFuse(this);
        minimumSpeed = MINIMUM_SPEED;
        airResistance.setCoefficient(MISSILE_AIR_RESISTANCE);
        receive(target);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        Information target = flightControl.getInformation();

        if(target == null || !(target.getSource() instanceof DestructibleObjectInterface))
            return;

        if(fuse.trigger((DestructibleObjectInterface) target.getSource()))
        {
            ((DestructibleObjectInterface) target.getSource()).takeDamage(getDamage());
            remove();
            AnimationManager.Add(TextAnimation.make("Hit", position, color));
        }

        // Less than minimum speed
        if(velocity.lengthSquared() < minimumSpeed * minimumSpeed)
        {
            System.out.println(velocity.lengthSquared());
            remove();
            AnimationManager.Add(TextAnimation.make("Missed", position, color));
        }
    }

    @Override
    public float getDamage() {
        return baseDamage;
    }

    @Override
    public void remove() {
        super.remove();
        System.out.println("Reasdsad");
    }
}
