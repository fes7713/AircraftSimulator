package aircraftsimulator.GameObject.Aircraft.Thruster;

import aircraftsimulator.GameObject.Aircraft.Aircraft;
import aircraftsimulator.GameObject.Aircraft.SwitchValueSimulator;

import javax.vecmath.Vector3f;

public class VariableThruster extends SimpleThruster implements SwitchValueSimulator {
    private final float minimumMagnitudePercentage;
    private final float maximumMagnitudePercentage;

    private ThrusterActionType thrusterActionType;

    public VariableThruster(Aircraft aircraft, float magnitude, float maximumMagnitudePercentage, float minimumMagnitudePercentage) {
        super(aircraft, magnitude);
        if(maximumMagnitudePercentage <= 1)
            throw new RuntimeException("Invalid max value");
        if(minimumMagnitudePercentage < 0 || minimumMagnitudePercentage > 1)
            throw new RuntimeException("Invalid percentage value");

        this.maximumMagnitudePercentage = maximumMagnitudePercentage;
        this.minimumMagnitudePercentage = minimumMagnitudePercentage;

    }

    @Override
    public Vector3f generateForce() {
        Vector3f force = super.generateForce();
        switch (thrusterActionType)
        {
            case ACCELERATION -> {
                force.scale(maximumMagnitudePercentage);
            }
            case DECELERATION -> {
                force.scale(minimumMagnitudePercentage);
            }
            case NORMAL -> {
                ;
            }
        }
        return force;
    }

    public void setThrusterActionType(ThrusterActionType type)
    {
        thrusterActionType = type;
    }

    @Override
    public Enum[] getSwitchCases() {
        return ThrusterActionType.values();
    }

    @Override
    public void simulateSwitchCase(ThrusterActionType type) {
        setThrusterActionType(type);
    }
}
