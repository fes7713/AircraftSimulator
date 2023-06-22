package aircraftsimulator.GameObject.Aircraft.FlightController;

import aircraftsimulator.GameObject.Aircraft.Aircraft;
import aircraftsimulator.GameObject.Aircraft.ForceApplier;
import aircraftsimulator.GameObject.Aircraft.SwitchTypeSimulator.SwitchTypesSimulator;

import javax.vecmath.Vector3f;
import java.util.*;

public class SwitchValueFlightController <E extends Enum<E>>extends AdvancedFlightController{
    private final List<SwitchTypesSimulator<E>> switchValuesList;
    private final Map<SwitchTypesSimulator<E>, E> timeMap;


    public SwitchValueFlightController(Aircraft parentObject, float interval) {
        super(parentObject, interval);
        timeMap = new HashMap<>();
        switchValuesList = new ArrayList<>();
        configurationChanged();
    }

    public SwitchValueFlightController(float interval) {
        super(interval);
        timeMap = new HashMap<>();
        switchValuesList = new ArrayList<>();
        configurationChanged();
    }

    public SwitchValueFlightController() {
        super();
        timeMap = new HashMap<>();
        switchValuesList = new ArrayList<>();
        configurationChanged();
    }

    @Override
    public Vector3f calculateLinearAcceleration(float delta) {
        timeMap.clear();
        for(SwitchTypesSimulator<E> sim: switchValuesList)
        {
            float timeMin = Float.MAX_VALUE;
            timeMap.put(sim, null);
            E[] types = sim.getSwitchTypes();
            for(E type : types)
            {
                sim.simulateSwitchTypes(type);
                Vector3f acceleration = super.calculateLinearAcceleration(delta);
                Vector3f velocity = new Vector3f();
                velocity.scaleAdd(delta, acceleration, parentObject.getVelocity());

                Vector3f hitPoint =  getTargetFuturePosition(delta, parentObject.getPosition(), velocity);
//                float time = timeToPoint(hitPoint);
//                Vector3f hitPoint = new Vector3f();
//                hitPoint.scaleAdd(delta, velocity, parentObject.getPosition());
//                hitPoint.sub(getTargetPosition(delta));
                // Length
                float time = hitPoint.lengthSquared();

                if(timeMin > time)
                {
                    timeMap.put(sim, type);
                    timeMin = time;
                }
            }
            sim.simulateSwitchTypes(timeMap.get(sim));
        }
        return super.calculateLinearAcceleration(delta);
    }

    public void configurationChanged()
    {
        switchValuesList.clear();
        if(parentObject != null){
            List<ForceApplier> forces = parentObject.getForceList();
            for(ForceApplier f: forces)
            {
                if(f instanceof SwitchTypesSimulator)
                    switchValuesList.add((SwitchTypesSimulator<E>) f);
            }
        }
    }
}
