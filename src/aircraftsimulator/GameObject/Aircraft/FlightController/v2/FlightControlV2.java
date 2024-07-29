package aircraftsimulator.GameObject.Aircraft.FlightController.v2;

import aircraftsimulator.GameObject.Component.Component;

import javax.vecmath.Vector3f;
import java.awt.*;

public abstract class FlightControlV2 extends Component {
    abstract public float calculateAngularAcceleration(float delta);
    abstract public Vector3f rotatedDirection(float radian);
    abstract public float getTargetAngle();
    abstract public void setTarget(Vector3f target);
    abstract public void draw(Graphics2D g2d);
}
