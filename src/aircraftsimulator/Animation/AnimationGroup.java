package aircraftsimulator.Animation;

import javax.vecmath.Vector2f;
import java.awt.*;
import java.util.LinkedList;

public class AnimationGroup extends Animation implements AnimationGroupInterface{
    protected final LinkedList<AnimationInterface> animations;

    public AnimationGroup(Vector2f position, Color color, float lifespan) {
        super(position, color, lifespan);
        animations = new LinkedList<>();
    }

    // TODO Multithread
    @Override
    public void update(float delta) {
        for(int i = 0; i < animations.size(); i++)
            animations.get(i).update(delta);
    }

    @Override
    public void draw(Graphics2D g2d) {
        for(int i = 0; i < animations.size(); i++)
            animations.get(i).draw(g2d);
    }

    @Override
    public void addAnimation(AnimationInterface animationInterface) {
        animations.addFirst(animationInterface);
    }

    @Override
    public void removeAnimation(AnimationInterface animationInterface) {
        animations.remove(animationInterface);
    }

    @Override
    public int size() {
        return animations.size();
    }
}
