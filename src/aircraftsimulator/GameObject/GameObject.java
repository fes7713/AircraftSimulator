package aircraftsimulator.GameObject;

import aircraftsimulator.Environment;
import aircraftsimulator.GameObject.Aircraft.Communication.Information.Information;
import aircraftsimulator.GameObject.Aircraft.Communication.Information.PositionInformationImp;
import aircraftsimulator.GameObject.Aircraft.Communication.SenderInterface;

import javax.vecmath.Vector3f;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GameObject implements GameObjectInterface, SenderInterface {
    protected final Team team;
    protected GameObject parent;
    protected final Vector3f position;
    protected final Color color;
    protected final float size;
    protected final List<Component> components;

    public GameObject(Team team, Vector3f position, Color color, float size)
    {
        this.team = team;
        this.position = position;
        this.color = color;
        this.size = size;
        components = new ArrayList<>();
    }

    @Override
    public Team getTeam() {
        return team;
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
    public void setParent(GameObject parent) {
        this.parent = parent;
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
    public void remove() {
        Environment.getInstance().removeObject(this);
    }

    @Override
    public <T extends Information> Information send(Class<T> type) {
        return new PositionInformationImp(this, position);
    }
}
