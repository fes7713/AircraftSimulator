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
    protected final float maxMagnitude;
    protected float magnitude;
    protected float fuel;
    protected float consumptionRate;

    protected ThrusterLevel level;

    protected final NetworkComponent networkComponent;

    public final static float CONSUMPTION_RATE = 1;
    public final static float BASE_CONSUMPTION_RATE = 10;

    public Thruster(Aircraft parent, float maxMagnitude, float fuel)
    {
        this.parent = parent;
        this.maxMagnitude = maxMagnitude;
        this.fuel = fuel;
        setMagnitude(ThrusterLevel.STOPPED);

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
        this.level = level;
        consumptionRate = BASE_CONSUMPTION_RATE * level.getPercentage() / ThrusterLevel.NORMAL.getPercentage();
        magnitude = maxMagnitude * level.getPercentage() / 100;
    }

    @Override
    public void update(float delta) {
        if(fuel < 0)
        {
            level = ThrusterLevel.STOPPED;
        }
        else if(fuel < delta * consumptionRate)
        {
            level = ThrusterLevel.STOPPED;
            fuel = 0;
        }
        else{
            fuel -= delta * consumptionRate;
        }
        parent.addForce(generateForce());
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
        return fuel / consumptionRate / getMagnitude();
    }

    public NetworkComponent getNetworkComponent() {
        return networkComponent;
    }

    @Override
    public float getMass() {
        return fuel;
    }
}
