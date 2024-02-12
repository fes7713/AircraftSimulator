package aircraftsimulator.GameObject.Aircraft.Communication.NetwrokAdaptor;

public class SampleNetworkAdapter implements NetworkAdaptor, DataProcessor{
    private final NetworkInterface networkInterface;

    public SampleNetworkAdapter()
    {
        this.networkInterface = new ResponsiveNetworkInterface(this);
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
