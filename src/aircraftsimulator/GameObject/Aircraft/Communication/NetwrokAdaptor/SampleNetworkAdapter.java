package aircraftsimulator.GameObject.Aircraft.Communication.NetwrokAdaptor;

import aircraftsimulator.GameObject.Aircraft.Communication.Event.Event;
import aircraftsimulator.GameObject.Aircraft.Communication.Event.Request.PingEvent;

public class SampleNetworkAdapter implements NetworkAdaptor, DataProcessor{
    private final NetworkInterface networkInterface;

    public SampleNetworkAdapter(NetworkInterface networkInterface)
    {
        this.networkInterface = networkInterface;
    }

    public SampleNetworkAdapter(String name, DataProcessor dataProcessor)
    {
        this.networkInterface = new ResponsiveNetworkInterface(name, dataProcessor);
    }

    public SampleNetworkAdapter(DataProcessor dataProcessor)
    {
        this.networkInterface = new ResponsiveNetworkInterface(dataProcessor);
    }

    @Override
    public NetworkInterface getNetworkInterface() {
        return networkInterface;
    }

    @Override
    public boolean process(Event data) {
        if(data instanceof PingEvent pingEvent)
        {
            getNetworkInterface().sendData(pingEvent.createReply(getNetworkInterface().getMac()));
        }


        return true;
    }
}
