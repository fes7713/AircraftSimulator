package aircraftsimulator.GameObject.Aircraft.Communication.Information;

import aircraftsimulator.GameObject.GameObjectInterface;
import aircraftsimulator.GameObject.PositionnInterface;

import javax.vecmath.Vector3f;
import java.awt.*;

public class LaserInformation implements Information, PositionnInterface {
    private final PositionInformation information;

    private final float frequency;
    private final float intensity;
    private final Vector3f direction;
    private final float angle;
    private final String code;
    private final Color color;

    public LaserInformation(PositionInformation info, float frequency, float intensity, Vector3f direction, float angle, String code, Color color) {
        this.information = info;
        this.frequency = frequency;
        this.intensity = intensity;
        this.direction = direction;
        this.angle = angle;
        this.code = code;
        this.color = color;
    }

    public float getFrequency() {
        return frequency;
    }

    public float getIntensity() {
        return intensity;
    }

    public Vector3f getDirection() {
        return direction;
    }

    public float getAngle() {
        return angle;
    }

    @Override
    public GameObjectInterface getSource() {
        return information.getSource();
    }

    public Information getInformation()
    {
        return information;
    }

    public String getCode()
    {
        return code;
    }

    @Override
    public Vector3f getPosition() {
        return information.getPosition();
    }

    public Color getColor()
    {
        return color;
    }
}
