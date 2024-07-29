package aircraftsimulator;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

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

    public static void main(String[] argv)
    {
        System.out.println(GameMath.directionToAngle(new Vector2f(1, 1.5F)));
        System.out.println(GameMath.sphereAreaForAngle(1, Math.toRadians(180)));
    }
}
