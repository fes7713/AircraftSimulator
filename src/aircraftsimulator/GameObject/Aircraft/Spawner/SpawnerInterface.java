package aircraftsimulator.GameObject.Aircraft.Spawner;

public interface  SpawnerInterface <T>{
    void spawn();
    T createObject();
    boolean trigger();
}
