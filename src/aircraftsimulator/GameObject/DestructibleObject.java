package aircraftsimulator.GameObject;

import javax.vecmath.Vector2f;
import java.awt.*;

public class DestructibleObject extends GameObject implements DestructibleObjectInterface{
    float health;

    public DestructibleObject(Vector2f position, Color color, float size, float health)
    {
        super(position, color, size);
        this.health = health;
    }

    public void destroy(){
        // TODO destroy object with animation
    }

    @Override
    public float getHealth() {
        return 0;
    }

    public boolean takeDamage(float damage){
        // TODO
        // Have indirect layer of damage calculation.
        // Consider having armor
        // Decorator pattern for armor
        // Reduce damage first by decorator pattern;
        return false;
    }
}
