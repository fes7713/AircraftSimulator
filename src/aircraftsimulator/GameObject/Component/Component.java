package aircraftsimulator.GameObject.Component;

public abstract class Component implements ComponentInterface, Cloneable{

    @Override
    public Component clone() {
        try {
            return (Component) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
