package aircraftsimulator.GameObject.Component;

import aircraftsimulator.GameObject.Aircraft.Communication.NetwrokAdaptor.DataProcessor;
import aircraftsimulator.GameObject.Aircraft.Communication.NetwrokAdaptor.NetworkInterface;
import aircraftsimulator.GameObject.Aircraft.Communication.NetwrokAdaptor.DefaultNetworkInterface;

public abstract class Component implements ComponentInterface, Cloneable, DataProcessor {
    private final NetworkInterface networkInterface;

    public Component()
    {
        networkInterface = new DefaultNetworkInterface(this, null);
    }

    @Override
    public Component clone() {
        try {
            return (Component) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
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
