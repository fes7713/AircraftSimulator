package aircraftsimulator.GameObject.Aircraft.Communication;

import aircraftsimulator.GameObject.Aircraft.Communication.Event.Event;
import aircraftsimulator.GameObject.Aircraft.Communication.Event.Request.ConfigurationChangeEvent;
import aircraftsimulator.GameObject.Aircraft.Communication.Event.Request.PingEvent;
import aircraftsimulator.GameObject.Aircraft.Communication.Event.Response.PingResponseEvent;
import aircraftsimulator.GameObject.Aircraft.Communication.NetwrokAdaptor.NetworkAdaptor;
import aircraftsimulator.GameObject.Aircraft.Communication.NetwrokAdaptor.NetworkInterface;
import aircraftsimulator.GameObject.Aircraft.Communication.NetwrokAdaptor.ResponsiveNetworkInterface;
import aircraftsimulator.GameObject.Aircraft.Communication.NetwrokAdaptor.SampleNetworkAdapter;

import java.util.*;

public class LocalRouter implements Router{
    private final Map<Integer, NetworkAdaptor> routingTable;
    private final Map<String, Integer> arpTable;
    private final NetworkAdaptor networkAdaptor;
    private Router parentRouter;

    public LocalRouter()
    {
        this("Default router");
    }

    public LocalRouter(String name)
    {
        routingTable = new HashMap<>();
        arpTable = new HashMap<>();
        networkAdaptor = new SampleNetworkAdapter(new ResponsiveNetworkInterface(name + " Interface",  this));

        addRouting(PortEnum.ITSELF, networkAdaptor);
    }

    public void addRouting(int port, NetworkAdaptor component){
        if(!routingTable.containsKey(port))
            routingTable.put(port, component);
        else
            System.out.printf("Port %d is occupied by %s\n", port, routingTable.get(port).getNetworkInterface().getMac());
    }

    public void removeRouting(int port)
    {
        NetworkAdaptor adaptor = routingTable.remove(port);
        System.out.printf("%s was disconnected from port %s\n", adaptor.getNetworkInterface().getMac(), port);

        ping(null);
    }

    public boolean update(float delta)
    {
        boolean result = getNetworkInterface().update(delta);
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
        return networkAdaptor.getNetworkInterface();
    }

    @Override
    public boolean process(Event event) {
        if(event instanceof PingEvent pingEvent)
        {
            // Prevent looping and ping to lower gen
            if(!pingEvent.getSourceMac().equals(getNetworkInterface().getMac()))
                ping(pingEvent);
            return true;
        }
        if(event instanceof ConfigurationChangeEvent confEvent)
        {
            if(parentRouter != null)
            {
                getNetworkInterface().sendData(
                        new ConfigurationChangeEvent(
                                askForPort(parentRouter.getNetworkInterface().getMac()),
                                getNetworkInterface().getMac()
                        )
                );
                ping();
            }
            else {
                ping();
            }
            return true;
        }

        if( event instanceof PingResponseEvent pingResponseEvent)
        {
            if(event.getDestinationMAC().equals(getNetworkInterface().getMac()))
            {
                // process
                //
                System.out.printf("[%s] processing\n", getNetworkInterface().getMac());
            }
            else{

            }
        }

        if(event.getDestinationMAC().equals(getNetworkInterface().getMac()))
        {
            // process
            //
            System.out.printf("[%s] processing\n", getNetworkInterface().getMac());
        }

//        else if(arpTable.containsKey(event.getDestinationMAC()))
//        {
//            int transferringPort = arpTable.get(event.getDestinationMAC());
//            System.out.println("Event transferring to port[" + transferringPort + "]");
//            getNetworkInterface().sendData(event);
//            return true;
//        }
        else{
            throw new RuntimeException("ARP error");
        }

        return false;
    }

    @Override
    public void ping(PingEvent parentPing) {
        arpTable.clear();
        for(Integer port: routingTable.keySet())
        {
            // Port number that adapter will send reply to
            NetworkAdaptor adaptor = routingTable.get(port);
            {
                if(parentPing == null)
                {
                    System.out.printf("[%s] ping from myself", this.getNetworkInterface().getMac());
                    adaptor.getNetworkInterface().sendData(new PingEvent(port, getNetworkInterface().getMac()));
                }else{
                    System.out.printf("[%s] ping from parent", this.getNetworkInterface().getMac());
                    adaptor.getNetworkInterface().sendData(new PingEvent(port, parentPing));
                }
            }
        }
    }

    @Override
    public void setParentRouter(Router router) {
        this.parentRouter = router;
    }

    @Override
    public void dispatchEvent(Event event) {
        if(event.getDestinationMAC() != null &&
                event.getDestinationMAC().equals(getNetworkInterface().getMac()))
            getNetworkInterface().receiveData(event);
    }

    public static void main(String[] args) throws InterruptedException {
        Router router = new LocalRouter("Router 1");
        Router router1 = new LocalRouter("Router 2");
        NetworkAdaptor adaptor1 = new SampleNetworkAdapter("Network Component 1", router);
        NetworkAdaptor adaptor2 = new SampleNetworkAdapter("Network Component 2", router);
        NetworkAdaptor adaptor3 = new SampleNetworkAdapter("Network Component 3", router1);

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
