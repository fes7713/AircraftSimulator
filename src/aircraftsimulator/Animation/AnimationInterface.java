package aircraftsimulator.Animation;

import javax.vecmath.Vector2f;
import java.awt.*;

public interface AnimationInterface {
    Vector2f getPosition();
    void update(float delta);
    void draw(Graphics2D g2d);
    void setAnimationEndSignal(AnimationEndSignal animationEndSignal);
}
