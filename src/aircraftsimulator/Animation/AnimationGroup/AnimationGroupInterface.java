package aircraftsimulator.Animation.AnimationGroup;

import aircraftsimulator.Animation.AnimationInterface;

public interface AnimationGroupInterface extends AnimationInterface {
    void addAnimation(AnimationInterface animationInterface);
    void removeAnimation(AnimationInterface animationInterface);
    int size();
}
