package aircraftsimulator.GameObject;

import javax.vecmath.Vector2f;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GameObject implements GameObjectInterface {
    Vector2f position;
    Color color;
    float size;

    List<Component> components;

    public GameObject(Vector2f position, Color color, float size)
    {
        this.position = position;
        this.color = color;
        this.size = size;

        components = new ArrayList<>();
    }

    public void addComponent(Component component)
    {
        components.add(component);
    }

    @Override
    public void update() {
        // TODO
    }


}
