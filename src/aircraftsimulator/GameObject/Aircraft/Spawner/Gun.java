package aircraftsimulator.GameObject.Aircraft.Spawner;

import aircraftsimulator.GameObject.Aircraft.Bullet;
import aircraftsimulator.GameObject.Aircraft.MovingObjectInterface;
import aircraftsimulator.GameObject.DestructibleObjectInterface;
import aircraftsimulator.GameObject.GameObject;

import javax.vecmath.Vector3f;

public class Gun extends TargetTimerSpawner<Bullet> {
    private final float bulletDamage;
    private final float bulletSpeed;

    public Gun(GameObject parent, float interval, float bulletDamage, float bulletSpeed) {
        super(parent, interval);
        this.bulletDamage = bulletDamage;
        this.bulletSpeed = bulletSpeed;
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
        return new Bullet(parent.getTeam(), (DestructibleObjectInterface)target.getSource(), (Vector3f) parent.getPosition().clone(), bulletVelocity, bulletDamage);
    }
}
