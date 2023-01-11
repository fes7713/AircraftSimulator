package aircraftsimulator.GameObject;

import java.awt.*;

public class Armor implements DestructibleObjectInterface{
    DestructibleObjectInterface object;


    public Armor(DestructibleObjectInterface object)
    {
        this.object = object;
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
    public boolean takeDamage(float damage) {
        // TODO
        // Reduce damage
        return false;
    }
}
