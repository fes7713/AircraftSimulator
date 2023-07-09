package aircraftsimulator.GameObject.Aircraft.Thruster;

import aircraftsimulator.GameObject.Aircraft.Aircraft;
import aircraftsimulator.GameObject.Aircraft.ForceApplier;
import aircraftsimulator.GameObject.Component.Component;
import aircraftsimulator.GameObject.GameObject;

import java.awt.*;

public abstract class Thruster extends Component implements ForceApplier{
    protected Aircraft parent;

    protected float fuel;
    protected final float fuelCoefficient;

    public final static float FUEL_COEFFICIENT = 1;

    public Thruster(Aircraft parent, float fuel)
    {
        this.parent = parent;
        this.fuel = fuel;
        fuelCoefficient = FUEL_COEFFICIENT;
    }

    public float getFuel()
    {
        return fuel;
    }

    @Override
    public void update(float delta) {
        fuel -= fuelCoefficient * delta;
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

    public float getMagnitude()
    {
        return 0;
    }

    public float getMaxTime()
    {
        if(fuel < 0)
            return 0;
        return fuel / fuelCoefficient;
    }

    @Override
    public Thruster clone() {
        return (Thruster) super.clone();
    }
}
