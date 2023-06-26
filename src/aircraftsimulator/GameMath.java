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

    public static void main(String[] argv)
    {
        System.out.println(GameMath.directionToAngle(new Vector2f(1, 1.5F)));
    }
}
