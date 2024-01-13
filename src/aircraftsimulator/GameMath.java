package aircraftsimulator;

import javax.vecmath.Vector2f;

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

    public static void main(String[] argv)
    {
        System.out.println(GameMath.directionToAngle(new Vector2f(1, 1.5F)));
        System.out.println(GameMath.sphereAreaForAngle(1, Math.toRadians(180)));
    }
}
