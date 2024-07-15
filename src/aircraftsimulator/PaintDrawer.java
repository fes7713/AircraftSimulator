package aircraftsimulator;

import aircraftsimulator.GameObject.Aircraft.Communication.Information.LaserInformation;
import aircraftsimulator.GameObject.Aircraft.Radar.Wave.ElectroMagneticWave;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import java.awt.*;

public class PaintDrawer {
    public static final Color reflectedColor = new Color(50, 50, 150, 200);
    public static final Color radarColor = new Color(71,179,77, 100);

    public static Color opacColor(Color color, float opacity)
    {
        float[] rgb = new float[4];
        color.getRGBComponents(rgb);
        return new Color(rgb[0], rgb[1], rgb[2], opacity * color.getAlpha() / 255);
    }

    public static void DrawLaser(Graphics2D g2d, LaserInformation laser, float opacity)
    {
        Vector3f direction = laser.getDirection();
        Vector3f center = laser.getPosition();
        float angle = laser.getAngle();
        float[] rgb = new float[4];
        laser.getColor().getRGBComponents(rgb);
        g2d.setColor(opacColor(laser.getColor(), opacity));
        double angleCos =
                Math.sqrt((direction.x * direction.x + direction.y * direction.y)
                        / (direction.x * direction.x + direction.y * direction.y + direction.z * direction.z));

        // TODO change intensity
        double length = laser.getIntensity() * angleCos;

        double centerAngle = GameMath.directionToAngle(new Vector2f(direction.x, direction.y)) % 360;

        g2d.fillArc((int)(center.x - length), (int)(center.y - length), (int)(length * 2), (int)(length * 2), (int)(centerAngle - angle / 2), (int)angle);
//        g2d.fillArc((int)(center.x - length / 4), (int)(center.y - length / 4), (int)(length / 2), (int)(length / 2), (int)(centerAngle - angle / 2), (int)angle);
    }

    public static void DrawPulse(Graphics2D g2d, ElectroMagneticWave wave)
    {

//        g2d.fillArc((int)(center.x - length / 4), (int)(center.y - length / 4), (int)(length / 2), (int)(length / 2), (int)(centerAngle - angle / 2), (int)angle);
    }
}
