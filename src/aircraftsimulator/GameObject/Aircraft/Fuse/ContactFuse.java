package aircraftsimulator.GameObject.Aircraft.Fuse;

import aircraftsimulator.GameObject.DestructibleObjectInterface;
import aircraftsimulator.GameObject.GameObjectInterface;

import javax.vecmath.Vector3f;

public class ContactFuse implements FuseInterface{
    private final GameObjectInterface parent;

    public ContactFuse(GameObjectInterface parent)
    {
        this.parent = parent;
    }

    @Override
    public boolean trigger(DestructibleObjectInterface target) {
        Vector3f myPos = parent.getPosition();
        Vector3f tagPos = target.getPosition();
        Vector3f distance = new Vector3f(myPos);
        distance.sub(tagPos);
        return ((parent.getSize() + target.getSize()) * (parent.getSize() + target.getSize())) > distance.lengthSquared();
    }
}
