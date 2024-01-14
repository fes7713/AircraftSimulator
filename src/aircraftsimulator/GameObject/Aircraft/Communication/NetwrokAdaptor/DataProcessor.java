package aircraftsimulator.GameObject.Aircraft.Communication.NetwrokAdaptor;

public interface DataProcessor {
    <E> boolean process(E data);
}
