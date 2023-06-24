package aircraftsimulator.GameObject.Aircraft.FlightController.LostControl;

import javax.vecmath.Vector3f;

public class PredictionControl extends LastSeenControl{
    public PredictionControl() {
        super();
    }

    @Override
    public void update(float delta)
    {
        if(lastSeenInformation == null)
            return;
        super.update(delta);
//        velocity.add(acceleration);
        Vector3f velocityScaled = new Vector3f(velocity);
        velocityScaled.scale(delta);

        position.add(velocityScaled);
    }
}
