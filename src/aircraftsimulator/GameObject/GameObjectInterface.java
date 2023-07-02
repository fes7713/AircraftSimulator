package aircraftsimulator.GameObject;

import javax.vecmath.Vector3f;
import java.awt.*;

public interface GameObjectInterface {
    Team getTeam();
    Vector3f getPosition();
    Color getColor();
    float getSize();
    void addComponent(Component component);
    void update(float delta);
    void draw(Graphics2D g2d);
    void remove();
}
