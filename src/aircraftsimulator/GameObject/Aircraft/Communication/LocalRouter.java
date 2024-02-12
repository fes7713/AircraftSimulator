package aircraftsimulator.GameObject.Aircraft.Communication;

import aircraftsimulator.GameObject.Aircraft.Communication.Event.Event;
import aircraftsimulator.GameObject.Aircraft.Communication.Event.EventPriority;
import aircraftsimulator.GameObject.Aircraft.Communication.Event.Request.PingEvent;
import aircraftsimulator.GameObject.Aircraft.Communication.NetwrokAdaptor.NetworkAdaptor;
import aircraftsimulator.GameObject.Aircraft.Communication.NetwrokAdaptor.NetworkInterface;
import aircraftsimulator.GameObject.Aircraft.Communication.NetwrokAdaptor.ResponsiveNetworkInterface;
import aircraftsimulator.GameObject.Aircraft.Communication.NetwrokAdaptor.SampleNetworkAdapter;

import java.util.*;

public class LocalRouter implements Router{
    private final String name;
    private final Map<Integer, List<NetworkAdaptor>> routingTable;
    private final Map<String, Integer> arpTable;
    private final NetworkInterface networkInterface;

    public LocalRouter()
    {
        this("Default router");
    }

    public LocalRouter(String name)
    {
        this.name = name;
        routingTable = new HashMap<>();
        arpTable = new HashMap<>();
        networkInterface = new ResponsiveNetworkInterface(name + " Interface",  this);
        addRouting(PortEnum.ITSELF, this);
    }

    public void addRouting(int port, NetworkAdaptor component){
        if(!routingTable.containsKey(port))
            routingTable.put(port, new ArrayList<>());
        routingTable.get(port).add(component);
        arpTable.put(component.getNetworkInterface().getMac(), port);

        if(getNetworkInterface().getRouter() != null)
        {
            int parentPort = getNetworkInterface().getRouter().askForPort(this.getMac());
            if(port != parentPort)
                getNetworkInterface().getRouter().addRouting(parentPort, component);
        }
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

    @Override
    public String getName() {
        return null;
    }

    public boolean update(float delta)
    {
        boolean result = networkInterface.update(delta);
        if(result)
        {

        }
        return result;
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
        if(event.getDestinationMAC() != null &&
                event.getDestinationMAC().equals(networkInterface.getMac()))
            networkInterface.receiveData(event);
    }

    @Override
    public void setRouter(Router router) {
        getNetworkInterface().setRouter(router);
    }

    @Override
    public Router getRouter() {
        return getNetworkInterface().getRouter();
    }

    @Override
    public String getMac() {
        return networkInterface.getMac();
    }

    @Override
    public Event popData() {
        return networkInterface.popData();
    }

    @Override
    public boolean process(Event event) {
        if(event instanceof PingEvent pingEvent)
        {
            // Prevent looping and ping to lower gen
            if(!pingEvent.getSourceMac().equals(getMac()))
                ping(pingEvent.getSourceMac());
            return true;
        }

        if(event.getDestinationMAC().equals(getMac()))
        {
            // process
            //
        }
        else if(arpTable.containsKey(event.getDestinationMAC()))
        {
            int transferringPort = arpTable.get(event.getDestinationMAC());
            System.out.println("Event transferring to port[" + transferringPort + "]");
            sendData(transferringPort, event);
            return true;
        }
        else{
            throw new RuntimeException("ARP error");
        }

        return false;
    }

    @Override
    public void ping() {
        for(Integer port: routingTable.keySet())
            for(NetworkAdaptor adaptor: routingTable.get(port))
            {
                adaptor.getNetworkInterface().receiveData(new PingEvent(port, networkInterface.getMac()));
            }
    }

    @Override
    public void ping(String sourceMac) {
        for(Integer port: routingTable.keySet())
            for(NetworkAdaptor adaptor: routingTable.get(port))
            {
                adaptor.getNetworkInterface().receiveData(new PingEvent(port, sourceMac));
            }
    }

    public static void main(String[] args) throws InterruptedException {
        Router router = new LocalRouter("Router 1");
        Router router1 = new LocalRouter("Router 2");
        NetworkAdaptor adaptor1 = new SampleNetworkAdapter("Network Component 1", router);
        NetworkAdaptor adaptor2 = new SampleNetworkAdapter("Network Component 2", router);
        NetworkAdaptor adaptor3 = new SampleNetworkAdapter(router1);

        router.addRouting(1, adaptor1);
        router.addRouting(2, adaptor2);
        router.addRouting(3, router1);
        router1.addRouting(1, adaptor3);

        router.ping();

        for(;;)
        {
            Thread.sleep(20);
            router.update(0.02F);
            router1.update(0.02F);
            adaptor1.getNetworkInterface().update(0.02F);
            adaptor2.getNetworkInterface().update(0.02F);
            adaptor3.getNetworkInterface().update(0.02F);
        }
    }

}
