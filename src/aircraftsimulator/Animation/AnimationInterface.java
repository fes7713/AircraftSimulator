package aircraftsimulator.Animation;

import java.awt.*;

public interface AnimationInterface {
    void update(float delta);
    void draw(Graphics2D g2d);
    void setAnimationEndSignal(AnimationEndSignal animationEndSignal);
}
