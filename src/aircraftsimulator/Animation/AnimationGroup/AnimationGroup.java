package aircraftsimulator.Animation.AnimationGroup;

import aircraftsimulator.Animation.Animation;
import aircraftsimulator.Animation.AnimationInterface;

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
        synchronized (this){
            for (AnimationInterface animation : animations) {
                animation.update(delta);
            }
        }

    }

    @Override
    public void draw(Graphics2D g2d) {
        synchronized (this){
            for (AnimationInterface animation : animations) {
                animation.draw(g2d);
            }
        }
    }

    @Override
    public void addAnimation(AnimationInterface animationInterface) {
        synchronized (this){
            animations.addFirst(animationInterface);
        }
    }

    @Override
    public void removeAnimation(AnimationInterface animationInterface) {
        synchronized (this){
            animations.remove(animationInterface);
        }
    }

    @Override
    public int size() {
        return animations.size();
    }
}
