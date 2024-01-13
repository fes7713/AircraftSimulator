package aircraftsimulator.GameObject.Aircraft.Communication;

import aircraftsimulator.GameObject.Aircraft.Communication.Event.Event;
import aircraftsimulator.GameObject.Aircraft.Communication.NetwrokAdaptor.NetworkAdaptor;

import java.util.*;

public class LocalRouter {
    Queue<Event<?>> eventQueue;
    Map<Integer, List<NetworkAdaptor>> routingTable;

    public LocalRouter()
    {
        routingTable = new HashMap<>();
        eventQueue = new ArrayDeque<>();
    }

    public void addRouting(int port, NetworkAdaptor component){
        if(!routingTable.containsKey(port))
            routingTable.put(port, new ArrayList<>());
        routingTable.get(port).add(component);
    }

    public void removeRouting(NetworkAdaptor component)
    {
        Set<Integer> ports = new HashSet<>(routingTable.keySet());
        for(Integer port: ports)
        {
            routingTable.get(port).remove(component);
            if(routingTable.get(port).isEmpty())
                routingTable.remove(port);
        }
    }

    public void addEvent(Event<?> event)
    {
        eventQueue.add(event);
    }

    public void update(float delta)
    {
        while(!eventQueue.isEmpty())
        {
            Event e = eventQueue.remove();
            int port = e.getPort();
            if(routingTable.containsKey(port))
            {
                routingTable.get(port).forEach(c -> c.getNetworkInterface().receiveData(e));
            }
        }
    }
}
