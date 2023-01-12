package aircraftsimulator.GameObject.MovePolicy;

import aircraftsimulator.GameObject.MovableObject;

import javax.vecmath.Vector2f;

public class SimpleMovePolicy implements MovePolicy{
    MovableObject parent;

    public SimpleMovePolicy(MovableObject parent)
    {
        this.parent = parent;
    }

    @Override
    public Vector2f destination() {
        // TODo
        return null;
    }
}
