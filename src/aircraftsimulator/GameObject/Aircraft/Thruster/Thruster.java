package aircraftsimulator.GameObject.Aircraft.Thruster;

import aircraftsimulator.GameObject.Aircraft.Aircraft;
import aircraftsimulator.GameObject.Aircraft.Communication.DataReceiver;
import aircraftsimulator.GameObject.Aircraft.Communication.NetworkComponent;
import aircraftsimulator.GameObject.Aircraft.Communication.SlowStartApplicationNetworkComponentImp;
import aircraftsimulator.GameObject.Aircraft.ForceApplier;
import aircraftsimulator.GameObject.Aircraft.SystemPort;
import aircraftsimulator.GameObject.Component.Component;
import aircraftsimulator.GameObject.GameObject;

import java.awt.*;

public abstract class Thruster extends Component implements ForceApplier, DataReceiver<ThrusterRequest> {
    protected Aircraft parent;
    protected float magnitude;
    protected float maxMagnitude;
    protected float fuel;
    protected final float fuelCoefficient;

    protected final NetworkComponent networkComponent;

    public final static float FUEL_COEFFICIENT = 1;

    public Thruster(Aircraft parent, float maxMagnitude, float fuel)
    {
        this.parent = parent;
        this.maxMagnitude = maxMagnitude;
        this.magnitude = ThrusterLevel.STOPPED.getPercentage();
        this.fuel = fuel;
        fuelCoefficient = FUEL_COEFFICIENT;

        networkComponent = new SlowStartApplicationNetworkComponentImp(parent.getNetwork());
        networkComponent.openPort(SystemPort.THRUSTER);
        networkComponent.addDataReceiver(ThrusterRequest.class, this);
    }

    public float getFuel()
    {
        return fuel;
    }

    public float getMagnitude() {
        return magnitude;
    }

    protected void setMagnitude(ThrusterLevel level){
        magnitude = maxMagnitude * level.getPercentage() / 100;
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
        parent.addAcceleration(generateForce());
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

    public NetworkComponent getNetworkComponent() {
        return networkComponent;
    }

    @Override
    public Thruster clone() {
        return (Thruster) super.clone();
    }
}
