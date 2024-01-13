package aircraftsimulator.GameObject.Component;

import aircraftsimulator.GameObject.Aircraft.Communication.NetwrokAdaptor.NetworkAdaptor;
import aircraftsimulator.GameObject.GameObject;

import java.awt.*;

public interface ComponentInterface extends NetworkAdaptor {
    void update(float delta);
    void draw(Graphics2D g2d);
    void setParent(GameObject parent);
}
