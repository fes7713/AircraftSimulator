package aircraftsimulator.GameObject.Aircraft.Radar.Detector;

import aircraftsimulator.GameObject.GameObject;

import java.awt.*;

public interface DetectorInterface {
    void detect();
    void setParent(GameObject parent);
    void update(float delta);
    void draw(Graphics2D g2d);
}
