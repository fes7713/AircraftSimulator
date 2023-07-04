package aircraftsimulator.Animation;

import aircraftsimulator.Animation.AnimationGroup.AnimationGroup;

public class AnimationManager extends AnimationGroup implements AnimationManagerInterface {

    private static AnimationManager instance;

    private AnimationManager() {
        super(null, null, 0);
    }

    public static AnimationManager getInstance()
    {
        if(instance == null)
            instance = new AnimationManager();
        return instance;
    }

    public static void Add(AnimationInterface animationInterface)
    {
        getInstance().addAnimation(animationInterface);
    }
}
