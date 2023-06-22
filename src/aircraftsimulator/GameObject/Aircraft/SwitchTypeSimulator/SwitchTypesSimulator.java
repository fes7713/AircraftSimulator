package aircraftsimulator.GameObject.Aircraft.SwitchTypeSimulator;

public interface SwitchTypesSimulator<E extends Enum<E>> {
    E[] getSwitchTypes();
    void simulateSwitchTypes(E type);
    E getCurrentType();
}
