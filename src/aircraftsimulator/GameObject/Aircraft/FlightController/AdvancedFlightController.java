package aircraftsimulator.GameObject.Aircraft.FlightController;

import aircraftsimulator.GameObject.Aircraft.Aircraft;

import javax.vecmath.Vector3f;

public class AdvancedFlightController extends SimpleFlightController{
    public AdvancedFlightController(Aircraft parentObject, float interval) {
        super(parentObject, interval);
    }

    public AdvancedFlightController(float interval) {
        super(interval);
    }

    public AdvancedFlightController() {
    }

    // Target is non-null in this context
    @Override
    public Vector3f getTargetFuturePosition(float delta, Vector3f position, Vector3f velocity) {
        Vector3f targetPosition = getTargetPosition();
        Vector3f targetVelocity = getTargetVelocity();

        if(targetVelocity == null)
            return targetPosition;

        Vector3f BA = new Vector3f(targetPosition);
        BA.sub(position);
        float a = velocity.lengthSquared() - targetVelocity.lengthSquared();
        float b = - 2 * (BA.x * targetVelocity.x + BA.y * targetVelocity.y + BA.z * targetVelocity.z);
        float c = - BA.lengthSquared();

        if(a == 0)
            return targetPosition;

        float determination = b * b - 4 * a * c;

        if(determination < 0)
        {
//            System.out.println("Desonot hit");
            return targetPosition;
        }
        else if(determination == 0)
        {
            System.out.println("Barely make it");
            return targetPosition;
        }
        else
        {
            float positiveTop = (float)(- b + Math.sqrt(determination));
            float timePositive = positiveTop / 2 / a;

            float negativeTop = (float)(- b -  Math.sqrt(determination));
            float timeNegative = negativeTop / 2 / a;

            float time;

            float timeSmaller = Math.min(timePositive, timeNegative);
            if(timeSmaller > 0)
                time = timeSmaller;
            else{
                float timeBigger = Math.max(timePositive, timeNegative);
                if(timeBigger > 0)
                    time = timeBigger;
                else
                    return targetPosition;
            }

            Vector3f r = new Vector3f();
            r.scaleAdd(time, targetVelocity, targetPosition);
            return r;
        }
    }
}
