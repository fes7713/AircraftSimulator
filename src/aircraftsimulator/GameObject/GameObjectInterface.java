package aircraftsimulator.GameObject;

import javax.vecmath.Vector2f;
import java.awt.*;

public interface GameObjectInterface {
    Vector2f getPosition();
    Color getColor();
    float getSize();
    void addComponent(Component component);
    void update();
}
