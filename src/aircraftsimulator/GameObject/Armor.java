package aircraftsimulator.GameObject;

import javax.vecmath.Vector2f;
import java.awt.*;

public class Armor implements DestructibleObjectInterface{
    DestructibleObjectInterface object;


    public Armor(DestructibleObjectInterface object)
    {
        this.object = object;
    }

    @Override
    public Vector2f getPosition() {
        return object.getPosition();
    }

    @Override
    public Color getColor() {
        return object.getColor();
    }

    @Override
    public float getSize() {
        return object.getSize();
    }

    @Override
    public void addComponent(Component component) {
        object.addComponent(component);
    }

    @Override
    public void update() {
        object.update();
    }

    @Override
    public float getHealth() {
        return object.getHealth();
    }

    @Override
    public boolean takeDamage(float damage) {
        // TODO
        // Reduce damage
        return false;
    }
}
