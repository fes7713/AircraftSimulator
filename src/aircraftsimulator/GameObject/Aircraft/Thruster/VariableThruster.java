package aircraftsimulator.GameObject.Aircraft.Thruster;

import aircraftsimulator.GameObject.Aircraft.Aircraft;
import aircraftsimulator.GameObject.Aircraft.SwitchTypeSimulator.SwitchTypesSimulator;

import javax.vecmath.Vector3f;
import java.util.Map;

import static aircraftsimulator.GameObject.Aircraft.Thruster.ThrusterActionType.*;

public class VariableThruster extends SimpleThruster implements SwitchTypesSimulator<ThrusterActionType> {
    private final Map<ThrusterActionType, Float> magnitudeMap;

    private ThrusterActionType thrusterActionType;

    public VariableThruster(Aircraft aircraft, float fuel, float magnitude, float maximumMagnitudePercentage, float minimumMagnitudePercentage) {
        super(aircraft, fuel, magnitude);
        if(maximumMagnitudePercentage < 1)
            throw new RuntimeException("Invalid max value");
        if(minimumMagnitudePercentage < 0 || minimumMagnitudePercentage > 1)
            throw new RuntimeException("Invalid percentage value");

        magnitudeMap = Map.of(ACCELERATION, maximumMagnitudePercentage, NORMAL, magnitude, DECELERATION, minimumMagnitudePercentage);
        thrusterActionType = ThrusterActionType.NORMAL;
    }

    public VariableThruster(Aircraft aircraft, float fuel, float magnitude)
    {
        this(aircraft, fuel, magnitude, magnitude, magnitude / 2F);
    }

    @Override
    public void update(float delta) {
        fuel -= fuelCoefficient *  magnitudeMap.get(thrusterActionType) * delta;
    }

    @Override
    public Vector3f generateForce() {
        Vector3f force = normalizedForce();
        force.scale(magnitudeMap.get(thrusterActionType));
        return force;
    }

    public void setThrusterActionType(ThrusterActionType type)
    {
        if(type == null)
            System.out.println("NULL");
        thrusterActionType = type;
    }

    @Override
    public float getMaxTime() {
        return super.getMaxTime() / magnitudeMap.get(NORMAL);
    }

    @Override
    public float getMagnitude() {
        return magnitudeMap.get(thrusterActionType);
    }

    public float getMagnitude(ThrusterActionType type) {
        return magnitudeMap.get(type);
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
