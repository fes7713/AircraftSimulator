package aircraftsimulator.GameObject.Aircraft.Thruster;

import aircraftsimulator.GameObject.Aircraft.Aircraft;
import aircraftsimulator.GameObject.Aircraft.SwitchTypeSimulator.SwitchTypesSimulator;

import javax.vecmath.Vector3f;

public class VariableThruster extends SimpleThruster implements SwitchTypesSimulator<ThrusterActionType> {
    private final float minimumMagnitudePercentage;
    private final float maximumMagnitudePercentage;

    private ThrusterActionType thrusterActionType;

    public VariableThruster(Aircraft aircraft, float magnitude, float maximumMagnitudePercentage, float minimumMagnitudePercentage) {
        super(aircraft, magnitude);
        if(maximumMagnitudePercentage < 1)
            throw new RuntimeException("Invalid max value");
        if(minimumMagnitudePercentage < 0 || minimumMagnitudePercentage > 1)
            throw new RuntimeException("Invalid percentage value");

        this.maximumMagnitudePercentage = maximumMagnitudePercentage;
        this.minimumMagnitudePercentage = minimumMagnitudePercentage;
        thrusterActionType = ThrusterActionType.NORMAL;
    }

    public VariableThruster(Aircraft aircraft, float magnitude)
    {
        this(aircraft, magnitude, magnitude, magnitude / 2F);
    }

    @Override
    public Vector3f generateForce() {
        Vector3f force = normalizedForce();
        switch (thrusterActionType)
        {
            case ACCELERATION -> force.scale(maximumMagnitudePercentage);
            case DECELERATION -> force.scale(minimumMagnitudePercentage);
            case NORMAL -> force.scale(magnitude);
        }
        return force;
    }

    public void setThrusterActionType(ThrusterActionType type)
    {
        if(type == null)
            System.out.println("NULL");
        thrusterActionType = type;
    }

    @Override
    public ThrusterActionType[] getSwitchTypes() {
        return ThrusterActionType.values();
    }

    @Override
    public void simulateSwitchTypes(ThrusterActionType type) {
        setThrusterActionType(type);
    }

    @Override
    public ThrusterActionType getCurrentType() {
        return thrusterActionType;
    }

    @Override
    public void setDefault() {
        setThrusterActionType(ThrusterActionType.NORMAL);
    }

    @Override
    public ThrusterActionType getDefault() {
        return ThrusterActionType.NORMAL;
    }
}
