package aircraftsimulator.GameObject.Component;

import aircraftsimulator.GameObject.GameObject;

public interface ObjectStorage {
    void store(GameObject object);
    GameObject request();
}
