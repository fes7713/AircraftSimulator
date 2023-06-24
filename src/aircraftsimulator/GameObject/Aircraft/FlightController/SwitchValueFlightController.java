package aircraftsimulator.GameObject.Aircraft.FlightController;

import aircraftsimulator.GameObject.Aircraft.Aircraft;
import aircraftsimulator.GameObject.Aircraft.ForceApplier;
import aircraftsimulator.GameObject.Aircraft.SwitchTypeSimulator.SwitchTypesSimulator;
import org.jetbrains.annotations.NotNull;

import javax.vecmath.Vector3f;
import java.util.*;

public class SwitchValueFlightController <E extends Enum<E>>extends AdvancedFlightController{
    private final List<SwitchTypesSimulator<E>> switchValuesList;
    private final Map<SwitchTypesSimulator<E>, E> lengthMap;


    public SwitchValueFlightController(Aircraft parentObject, float interval) {
        super(parentObject, interval);
        lengthMap = new HashMap<>();
        switchValuesList = new ArrayList<>();
        configurationChanged();
    }

    public SwitchValueFlightController(float interval) {
        super(interval);
        lengthMap = new HashMap<>();
        switchValuesList = new ArrayList<>();
        configurationChanged();
    }

    public SwitchValueFlightController() {
        super();
        lengthMap = new HashMap<>();
        switchValuesList = new ArrayList<>();
        configurationChanged();
    }

    protected float calculateLength(SwitchTypesSimulator<E> switchType, float delta, @NotNull Vector3f position, Vector3f velocity)
    {
        return position.lengthSquared();
    }

    @Override
    public Vector3f calculateLinearAcceleration(float delta) {
        lengthMap.clear();

        for(SwitchTypesSimulator<E> sim: switchValuesList)
        {
            if(target == null)
            {
                sim.setDefault();
                continue;
            }
            float lengthMin = Float.MAX_VALUE;
            lengthMap.put(sim, null);
            E[] types = sim.getSwitchTypes();
            for(E type : types)
            {
                sim.simulateSwitchTypes(type);
                Vector3f acceleration = parentObject.getAcceleration();
                Vector3f velocity = new Vector3f();
                velocity.scaleAdd(delta, acceleration, parentObject.getVelocity());
                Vector3f position = new Vector3f();
                position.scaleAdd(delta, velocity, parentObject.getPosition());
                Vector3f hitPoint =  getTargetFuturePosition(delta, position, velocity);

                position.sub(hitPoint);
                // Length
                float length = calculateLength(sim, delta, position, velocity);

                if(lengthMin > length)
                {
                    lengthMap.put(sim, type);
                    lengthMin = length;
                }
            }
            sim.simulateSwitchTypes(lengthMap.get(sim));
        }
        return parentObject.getAcceleration();
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
