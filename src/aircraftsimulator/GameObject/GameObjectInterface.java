package aircraftsimulator.GameObject;

import aircraftsimulator.GameObject.Aircraft.Communication.Router;
import aircraftsimulator.GameObject.Component.Component;

import java.awt.*;

public interface GameObjectInterface extends PositionnInterface{
    String getId();

    Team getTeam();
    Color getColor();
    void setParent(GameObject parent);
    float getSize();
    void addComponent(Component component);
    void update(float delta);
    void componentUpdate(float delta);
    void draw(Graphics2D g2d);
    void remove();
    Router getRouter();

    float getRCS();
    float getSurfaceRoughness();
}
