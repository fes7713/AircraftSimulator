package aircraftsimulator.Animation;

public interface AnimationGroupInterface extends AnimationInterface{
    void addAnimation(AnimationInterface animationInterface);
    void removeAnimation(AnimationInterface animationInterface);
    int size();
}
