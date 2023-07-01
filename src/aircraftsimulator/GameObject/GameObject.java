package aircraftsimulator.GameObject;

import aircraftsimulator.GameObject.Aircraft.Communication.Information.Information;
import aircraftsimulator.GameObject.Aircraft.Communication.Information.PositionInformation;
import aircraftsimulator.GameObject.Aircraft.Communication.SenderInterface;

import javax.vecmath.Vector3f;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GameObject implements GameObjectInterface, SenderInterface {
    protected Vector3f position;
    protected Color color;
    protected float size;
    protected List<Component> components;

    public GameObject(Vector3f position, Color color, float size)
    {
        this.position = position;
        this.color = color;
        this.size = size;
        components = new ArrayList<>();
    }

    @Override
    public Vector3f getPosition() {
        return position;
    }

    @Override
    public Color getColor() {
        return color;
    }

    @Override
    public float getSize() {
        return size;
    }

    public void addComponent(Component component)
    {
        components.add(component);
    }

    @Override
    public void update(float delta) {
        // TODO
    }

    @Override
    public void draw(Graphics2D g2d) {
        g2d.setColor(color);
        g2d.fillOval((int)(position.x - size /2), (int)(position.y - size /2), (int)size, (int)size);
    }

    @Override
    public <T extends Information> Information send(Class<T> type) {
        return new PositionInformation(this, position);
    }
}
