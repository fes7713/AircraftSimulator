package aircraftsimulator.GameObject.Aircraft.Thruster;

import aircraftsimulator.GameObject.Aircraft.Aircraft;
import aircraftsimulator.GameObject.Aircraft.ForceApplier;
import aircraftsimulator.GameObject.Component.Component;
import aircraftsimulator.GameObject.GameObject;

import java.awt.*;

public abstract class Thruster extends Component implements ForceApplier{
    protected Aircraft parent;
    protected float magnitude;
    protected float fuel;
    protected final float fuelCoefficient;

    public final static float FUEL_COEFFICIENT = 1;

    public Thruster(Aircraft parent, float magnitude, float fuel)
    {
        this.parent = parent;
        this.magnitude = magnitude;
        this.fuel = fuel;
        fuelCoefficient = FUEL_COEFFICIENT;
    }

    public float getFuel()
    {
        return fuel;
    }

    public float getMagnitude() {
        return magnitude;
    }

    @Override
    public void update(float delta) {
        if(fuel == 0)
        {
            magnitude = 0;
        }
        else if(fuel < getMagnitude() * delta * fuelCoefficient)
        {
            magnitude = fuel / delta / fuelCoefficient;
            fuel = 0;
        }
        else{
            magnitude = getMagnitude();
            fuel -= magnitude * delta * fuelCoefficient;
        }
    }

    @Override
    public void draw(Graphics2D g2d) {

    }

    @Override
    public void setParent(GameObject parent) {
        if(parent instanceof Aircraft aircraft)
            this.parent = aircraft;
        else
            throw new RuntimeException("Error in Simple Thruster");
    }

    public float getMaxTime()
    {
        return fuel / fuelCoefficient / getMagnitude();
    }

    @Override
    public Thruster clone() {
        return (Thruster) super.clone();
    }
}
