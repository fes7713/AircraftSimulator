package aircraftsimulator.GameObject.Aircraft.FlightController.LostControl;

import aircraftsimulator.GameObject.Aircraft.Communication.Information.Information;
import aircraftsimulator.GameObject.Aircraft.Communication.Information.MotionInformation;
import aircraftsimulator.GameObject.Aircraft.Communication.Information.PositionInformation;

import javax.vecmath.Vector3f;

public class LastSeenControl implements LostControlInterface{
    protected Information lastSeenInformation;
    private float lostTime;
    protected Vector3f acceleration;
    protected Vector3f velocity;
    protected Vector3f position;

    public LastSeenControl()
    {
        setInformation(null);
    }

    @Override
    public void update(float delta)
    {
        if(lastSeenInformation == null)
            return;
        lostTime += delta;
    }

    @Override
    public void setInformation(Information information)
    {
        lastSeenInformation = information;
        if(lastSeenInformation instanceof MotionInformation info)
        {
            position = info.getPosition();
            velocity = info.getVelocity();
            acceleration = info.getAcceleration();
        }
        else if(lastSeenInformation instanceof PositionInformation info)
        {
            position = info.getPosition();
            velocity = new Vector3f();
            acceleration = new Vector3f();
        }
        else{
            lastSeenInformation = null;
        }
        lostTime = 0;
    }

    @Override
    public Vector3f getPosition()
    {
        return position;
    }

    @Override
    public Vector3f getVelocity()
    {
        return velocity;
    }

    @Override
    public float getLostTime()
    {
        return lostTime;
    }

    @Override
    public void disable() {
        setInformation(null);
    }

    @Override
    public boolean isLost() {
        return lastSeenInformation != null;
    }
}