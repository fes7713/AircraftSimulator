package aircraftsimulator.GameObject.Aircraft.Communication;

import aircraftsimulator.GameObject.Aircraft.Communication.Event.Event;
import aircraftsimulator.GameObject.Aircraft.Communication.Event.EventPriority;
import aircraftsimulator.GameObject.Aircraft.Communication.Event.Request.PingEvent;
import aircraftsimulator.GameObject.Aircraft.Communication.NetwrokAdaptor.*;

import java.util.*;

public class LocalRouter implements Router{
    private final Queue<Event<?>> eventQueue;
    private final Map<Integer, List<NetworkAdaptor>> routingTable;
    private final Map<String, Integer> arpTable;
    private final NetworkInterface networkInterface;
    private Router router;

    public LocalRouter()
    {
        routingTable = new HashMap<>();
        eventQueue = new ArrayDeque<>();
        arpTable = new HashMap<>();
        networkInterface = new DefaultNetworkInterface(this, this);
        addRouting(PortEnum.ITSELF, this);
    }

    public void addRouting(int port, NetworkAdaptor component){
        if(!routingTable.containsKey(port))
            routingTable.put(port, new ArrayList<>());
        routingTable.get(port).add(component);
        arpTable.put(component.getNetworkInterface().getMac(), port);
        component.getNetworkInterface().setRouter(this);
    }

    public void removeRouting(NetworkAdaptor component)
    {
        arpTable.remove(component.getNetworkInterface().getMac());
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
        networkInterface.update(delta);
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

    @Override
    public int askForPort(String destinationMac) {
        return arpTable.getOrDefault(destinationMac, PortEnum.DEFAULT_GATEWAY);
    }

    @Override
    public NetworkInterface getNetworkInterface() {
        return networkInterface;
    }


    @Override
    public void sendData(int port, Object data, EventPriority priority) {
        if(routingTable.containsKey(port))
        {
            List<NetworkAdaptor> adaptors = routingTable.get(port);
            for(int i = 0; i < adaptors.size(); i++)
            {
                NetworkInterface networkInterface = adaptors.get(i).getNetworkInterface();

            }
        }
    }

    @Override
    public void receiveData(Event event) {
        networkInterface.receiveData(event);
    }

    @Override
    public void setRouter(Router router) {
        this.router = router;
        if(router != null)
            routingTable.put(PortEnum.DEFAULT_GATEWAY, new LinkedList<>(Arrays.asList(router)));
        else
            routingTable.remove(PortEnum.DEFAULT_GATEWAY);
    }

    @Override
    public String getMac() {
        return networkInterface.getMac();
    }

    @Override
    public float getProcessTime() {
        return 0;
    }

    @Override
    public NetworkInterfaceMode getNetworkMode() {
        return null;
    }

    @Override
    public void ping() {
        for(Integer port: routingTable.keySet())
            for(NetworkAdaptor adaptor: routingTable.get(port))
            {
                adaptor.getNetworkInterface().receiveData(new PingEvent(port, networkInterface.getMac()));
            }
    }

    public static void main(String[] args) throws InterruptedException {
        Router router = new LocalRouter();
        NetworkAdaptor adaptor1 = new SampleNetworkAdapter(router);
        NetworkAdaptor adaptor2 = new SampleNetworkAdapter(router);

        router.addRouting(1, adaptor1);
        router.addRouting(2, adaptor2);

        router.ping();

        for(;;)
        {
            Thread.sleep(20);
            router.update(0.02F);
            adaptor1.getNetworkInterface().update(0.02F);
            adaptor2.getNetworkInterface().update(0.02F);
        }
    }

    @Override
    public <E> boolean process(E data) {
        return false;
    }
}
