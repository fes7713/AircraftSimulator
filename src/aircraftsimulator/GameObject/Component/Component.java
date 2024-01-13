package aircraftsimulator.GameObject.Component;

import aircraftsimulator.GameObject.Aircraft.Communication.NetwrokAdaptor.BasicNetworkInterface;
import aircraftsimulator.GameObject.Aircraft.Communication.NetwrokAdaptor.NetworkInterface;

public abstract class Component implements ComponentInterface, Cloneable{
    private NetworkInterface networkInterface;

    public Component()
    {
        networkInterface = new BasicNetworkInterface();
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
}
