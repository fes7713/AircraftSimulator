package aircraftsimulator.GameObject;

import aircraftsimulator.Environment;
import aircraftsimulator.GameObject.Aircraft.Communication.Information.LaserInformation;
import aircraftsimulator.GameObject.Aircraft.Communication.Information.PositionInformation;
import aircraftsimulator.GameObject.Aircraft.Communication.Information.PositionInformationImp;
import aircraftsimulator.GameObject.Aircraft.Communication.LocalRouter;
import aircraftsimulator.GameObject.Aircraft.Communication.SenderInterface;
import aircraftsimulator.GameObject.Component.Component;
import aircraftsimulator.PaintDrawer;

import javax.vecmath.Vector3f;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GameObject implements GameObjectInterface, SenderInterface, ReflectionInterface {
    protected final Team team;
    protected GameObject parent;
    protected final Vector3f position;
    protected final Color color;
    protected final float size;
    protected final List<Component> components;
    protected final LocalRouter router;

    // 0 to 1
    protected float surfaceRoughness;

    public GameObject(Team team, Vector3f position, Color color, float size)
    {
        this.team = team;
        this.position = position;
        this.color = color;
        this.size = size;
        components = new ArrayList<>();
        router = new LocalRouter();
        surfaceRoughness = 0.5F;
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
        router.update(delta);
    }

    public void componentUpdate(float delta){
        components.forEach(o -> o.update(delta));
    }

    @Override
    public void draw(Graphics2D g2d) {
        components.forEach(o -> o.draw(g2d));
        g2d.setColor(color);
        g2d.fillOval((int)(position.x - size /2), (int)(position.y - size /2), (int)size, (int)size);
    }

    @Override
    public void remove() {
        Environment.getInstance().removeObject(this);
    }

    @Override
    public LocalRouter getRouter() {
        return router;
    }

    @Override
    public <T extends PositionInformation> PositionInformation send(Class<T> type) {
        return new PositionInformationImp(this, position);
    }

    @Override
    public LaserInformation reflect(Vector3f source, float frequency, float intensity) {
        Vector3f reflectedDirection = new Vector3f(source);
        reflectedDirection.sub(position);
        // TODO
        float angle = 20;
        return new LaserInformation(new PositionInformationImp(this, position), frequency, intensity, reflectedDirection, angle, team.getTeamName(), PaintDrawer.reflectedColor);
    }
}
