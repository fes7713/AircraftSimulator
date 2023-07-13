package aircraftsimulator.GameObject.Aircraft.FlightController.LostControl;

import aircraftsimulator.GameObject.Aircraft.Communication.Information.Information;
import aircraftsimulator.GameObject.Aircraft.Communication.Information.MotionInformation;
import aircraftsimulator.GameObject.Aircraft.Communication.Information.MotionInformationImp;

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
////        velocity.add(acceleration);
//        Vector3f velocityScaled = new Vector3f(velocity);
//        velocityScaled.scale(delta);
//
//        position.add(velocityScaled);
    }

    @Override
    public Information getTarget() {

        if(lastSeenInformation instanceof MotionInformation info) {
            Vector3f velocityScaled = new Vector3f(info.getVelocity());
            velocityScaled.scale(getLostTime());

            Vector3f position = new Vector3f(info.getPosition());
            position.add(velocityScaled);
            // TODO May need to fix the assumption where speed and direction does not change.
            return new MotionInformationImp(info.getSource(), position, info.getVelocity(), info.getAcceleration(), info.getDirection());
        }

        return lastSeenInformation;
    }
}
