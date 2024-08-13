package aircraftsimulator.GameObject.Component;

import aircraftsimulator.GameObject.Aircraft.Communication.NetworkComponent;
import aircraftsimulator.GameObject.GameObject;

import java.awt.*;

public interface ComponentInterface {
    void update(float delta);
    void draw(Graphics2D g2d);
    void setParent(GameObject parent);
    void setup();
    NetworkComponent getNetworkComponent();
}
