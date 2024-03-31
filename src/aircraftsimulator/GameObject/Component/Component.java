package aircraftsimulator.GameObject.Component;

public abstract class Component implements ComponentInterface, Cloneable {
//    private final NetworkInterface networkInterface;

    public Component()
    {
//        networkInterface = new ResponsiveNetworkInterface(this);
    }

    @Override
    public Component clone() {
        try {
            return (Component) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
