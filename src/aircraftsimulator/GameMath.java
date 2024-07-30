package aircraftsimulator;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import java.util.List;

public class GameMath {
    public static double directionToAngle(Vector2f direction)
    {
        double angle = - Math.toDegrees(Math.atan(direction.y / direction.x));

        if(direction.x >= 0)
        {
            return (360 + angle) % 360;
        }
        else
        {
            return (180 + angle) % 360;
        }
    }

    public static double sphereAreaForAngle(float radius, double angleInRad)
    {
        return 2 * Math.PI * (1 - Math.cos(angleInRad)) * radius * radius;
    }

    public static double getCosAngleToHorizontal(Vector3f direction)
    {
        Vector3f horizontalVector = new Vector3f(direction.x, direction.y, 0);
        return direction.dot(horizontalVector) / direction.length() / horizontalVector.length();
    }

    public static boolean isWithinAngle(Vector3f direction1, Vector3f direction2, float direction2Length, float angle)
    {
        float angleCos = direction1.dot(direction2) / direction1.length() / direction2Length;
        return angleCos > Math.cos(Math.toRadians(angle));
    }

    public static Vector3f rotatedDirection(float radian, Vector3f direction, Vector3f destinationVector)
    {
        Vector3f n = new Vector3f();
        n.cross(direction, destinationVector);
        if(n.lengthSquared() == 0)
            n.set(0, 0, 1);
        n.normalize();
        Matrix3f rotationMatrix = new Matrix3f(
                n.x * n.x, n.x * n.y, n.x * n.z,
                n.y * n.x, n.y * n.y, n.y * n.z,
                n.z * n.x, n.z * n.y, n.z * n.z);
        rotationMatrix.mul((float)(1 - Math.cos(radian)));
        rotationMatrix.add(new Matrix3f(
                (float)Math.cos(radian), (float)(- n.z * Math.sin(radian)), (float)(n.y * Math.sin(radian)),
                (float)(n.z * Math.sin(radian)), (float)Math.cos(radian), (float)(-n.x * Math.sin(radian)),
                (float)(- n.y * Math.sin(radian)), (float)(n.x * Math.sin(radian)), (float)Math.cos(radian))
        );
        Vector3f r = new Vector3f();
        rotationMatrix.transform(direction, r);
        r.normalize();
        return r;
    }

    public static Vector3f getVelocityVector(Vector3f lastPosition, Vector3f secondLastPosition, float timeDiff)
    {
        Vector3f diffA = new Vector3f(lastPosition);
        diffA.sub(secondLastPosition);
        diffA.scale(1/timeDiff);
        return diffA;
    }

    public static Vector3f getAccelerationVector(Vector3f lastVelocity, Vector3f secondLastVelocity, float timeDiff)
    {
        return  getVelocityVector(lastVelocity, secondLastVelocity, timeDiff);
    }

    public static Vector3f getAccelerationVector(Vector3f lastPosition, Vector3f secondLastPosition, Vector3f thirdLastPosition, float timeDiff)
    {
        Vector3f diffA = getVelocityVector(lastPosition, secondLastPosition, timeDiff);
        Vector3f diffB = getVelocityVector(secondLastPosition, thirdLastPosition, timeDiff);

        return  getAccelerationVector(diffA, diffB, timeDiff);
    }

    public static float additiveAverage(List<Float> values)
    {
        float valueSum = 0;
        int weightSum = 0;
        for(int i = 0; i < values.size(); i++)
        {
            valueSum += (i + 1) * values.get(i);
            weightSum += i + 1;
        }
        return valueSum / weightSum;
    }

    public static void main(String[] argv)
    {
        System.out.println(GameMath.directionToAngle(new Vector2f(1, 1.5F)));
        System.out.println(GameMath.sphereAreaForAngle(1, Math.toRadians(180)));
    }
}
