package aircraftsimulator.GameObject.Aircraft.Fuse;

import aircraftsimulator.GameObject.DestructibleObjectInterface;

public interface FuseInterface {
    boolean trigger(DestructibleObjectInterface target);
}
