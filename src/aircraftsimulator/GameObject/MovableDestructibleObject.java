package aircraftsimulator.GameObject;

import javax.vecmath.Vector2f;
import java.awt.*;

public class MovableDestructibleObject extends MovableObject implements DestructibleObjectInterface{
    public MovableDestructibleObject(Vector2f position, Color color, float size) {
        super(position, color, size);
    }

    @Override
    public float getHealth() {
        return 0;
    }

    @Override
    public boolean takeDamage(float damage) {
        return false;
    }
}
