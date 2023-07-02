package aircraftsimulator.GameObject.Aircraft;

import aircraftsimulator.GameObject.Aircraft.Communication.Information.Information;
import aircraftsimulator.GameObject.Aircraft.Communication.Information.PositionInformation;
import aircraftsimulator.GameObject.Aircraft.Communication.ReceiverInterface;
import aircraftsimulator.GameObject.Aircraft.Spawner.TimeSpawner;
import aircraftsimulator.GameObject.DestructibleObjectInterface;
import aircraftsimulator.GameObject.GameObject;
import org.jetbrains.annotations.Nullable;

import javax.vecmath.Vector3f;

public class Gun extends TimeSpawner<Bullet> implements ReceiverInterface {
    private PositionInformation target;
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

    @Override
    public void receive(@Nullable Information information) {
        if(information == null)
        {
            this.target = null;
            return;
        }
        if(information.getSource() instanceof DestructibleObjectInterface)
            this.target = (PositionInformation) information;
    }
}
