package aircraftsimulator.GameObject;

import javax.vecmath.Vector3f;
import java.awt.*;

public class Armor implements DestructibleObjectInterface{
    DestructibleObjectInterface object;


    public Armor(DestructibleObjectInterface object)
    {
        this.object = object;
    }

    @Override
    public Vector3f getPosition() {
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
    public void update(float delta) {
        object.update(delta);
    }

    @Override
    public void draw(Graphics2D g2d) {
        object.draw(g2d);
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
