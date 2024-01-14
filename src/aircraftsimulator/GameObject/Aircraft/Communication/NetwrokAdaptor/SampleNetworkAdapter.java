package aircraftsimulator.GameObject.Aircraft.Communication.NetwrokAdaptor;

import aircraftsimulator.GameObject.Aircraft.Communication.Router;

public class SampleNetworkAdapter implements NetworkAdaptor, DataProcessor{
    private final NetworkInterface networkInterface;

    public SampleNetworkAdapter(Router router)
    {
        this.networkInterface = new DefaultNetworkInterface(this, router);
    }

    @Override
    public NetworkInterface getNetworkInterface() {
        return networkInterface;
    }

    @Override
    public <E> boolean process(E data) {
        return false;
    }
}
