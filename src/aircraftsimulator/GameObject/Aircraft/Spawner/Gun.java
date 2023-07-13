package aircraftsimulator.GameObject.Aircraft.Spawner;

import aircraftsimulator.GameObject.Aircraft.Bullet;
import aircraftsimulator.GameObject.Aircraft.Communication.Information.FireInformation;
import aircraftsimulator.GameObject.Aircraft.MovingObjectInterface;
import aircraftsimulator.GameObject.DestructibleObjectInterface;
import aircraftsimulator.GameObject.GameObject;

import javax.vecmath.Vector3f;

public class Gun extends TargetTimerSpawner<Bullet> implements CloseRangeWeaponSystem {
    private final float bulletSpeed;

    private final Bullet sample;

    public Gun(GameObject parent, Bullet sample, float interval, float bulletSpeed) {
        super(parent, interval);
        this.bulletSpeed = bulletSpeed;

        this.sample = sample;
    }

    public Gun(GameObject parent, float interval, float bulletDamage, float bulletSpeed) {
        super(parent, interval);
        this.bulletSpeed = bulletSpeed;

        sample = new Bullet(parent.getTeam(), bulletDamage);
    }

    @Override
    public Bullet createObject() {
        if(target == null)
            return null;

        Vector3f bulletVelocity;
        if(parent instanceof MovingObjectInterface)
            bulletVelocity= new Vector3f(((MovingObjectInterface)parent).getDirection());
        else
        {
            bulletVelocity= new Vector3f(target.getPosition());
            bulletVelocity.sub(parent.getPosition());
        }
        bulletVelocity.normalize();
        bulletVelocity.scale(bulletSpeed);
        if(parent instanceof MovingObjectInterface m)
            bulletVelocity.add(m.getVelocity());
        Bullet clone = sample.clone();
        clone.activate(parent.getPosition(), bulletVelocity, bulletVelocity);
        clone.setTarget((DestructibleObjectInterface)target.getSource());
        return clone;
    }

    @Override
    public float getRange() {
        if(parent instanceof MovingObjectInterface m)
            sample.getVelocity().set(bulletSpeed + m.getVelocity().length(), 0, 0);
        else
            sample.getVelocity().set(bulletSpeed, 0, 0);
        return sample.getRange();
    }

    @Override
    public void fire(FireInformation fireInformation) {
        receive(fireInformation);
    }

    // TODO mau need to change later
    @Override
    public boolean isAvailable() {
        return true;
    }
}
