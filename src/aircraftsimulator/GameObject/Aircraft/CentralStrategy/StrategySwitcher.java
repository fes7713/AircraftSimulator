package aircraftsimulator.GameObject.Aircraft.CentralStrategy;

import aircraftsimulator.GameObject.Aircraft.CentralStrategy.Event.StrategyState;
import aircraftsimulator.GameObject.Aircraft.CentralStrategy.Event.Event;
import aircraftsimulator.GameObject.Aircraft.CentralStrategy.Event.EventHandler;

import java.util.HashMap;
import java.util.Map;

public class StrategySwitcher {
    private final Map<Event, Map<StrategyState, EventHandler>> handlerMap;

    public StrategySwitcher()
    {
        handlerMap = new HashMap<>();
    }

    public void addHandler(Event event, StrategyState state, EventHandler handler)
    {
        if(!handlerMap.containsKey(event))
            handlerMap.put(event, new HashMap<>());
        if(!handlerMap.get(event).containsKey(state))
            handlerMap.get(event).put(state, handler);
        else
            throw new IllegalArgumentException("Cannot override event");
    }

    public StrategyState handleEvent(Event event, StrategyState state)
    {
        if(!handlerMap.containsKey(event) || !handlerMap.get(event).containsKey(state))
            throw new IllegalArgumentException("Invalid event and state pair");
        return handlerMap.get(event).get(state).handle();
    }
}
