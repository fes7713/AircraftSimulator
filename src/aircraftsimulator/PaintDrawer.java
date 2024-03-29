package aircraftsimulator;

import aircraftsimulator.GameObject.Aircraft.Communication.Information.LaserInformation;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import java.awt.*;

public class PaintDrawer {
    public static final Color reflectedColor = new Color(50, 50, 150, 100);
    public static final Color radarColor = new Color(71,179,77, 50);;

    public static void DrawLaser(Graphics2D g2d, LaserInformation laser)
    {
        Vector3f direction = laser.getDirection();
        Vector3f center = laser.getPosition();
        float angle = laser.getAngle();

        g2d.setColor(laser.getColor());
        double angleCos =
                Math.sqrt((direction.x * direction.x + direction.y * direction.y)
                        / (direction.x * direction.x + direction.y * direction.y + direction.z * direction.z));

        // TODO change intensity
        double length = laser.getIntensity() * angleCos;

        double centerAngle = GameMath.directionToAngle(new Vector2f(direction.x, direction.y)) % 360;

        g2d.fillArc((int)(center.x - length), (int)(center.y - length), (int)(length * 2), (int)(length * 2), (int)(centerAngle - angle / 2), (int)angle);
    }
}
