package aircraftsimulator.GameObject.Component;

import aircraftsimulator.GameObject.GameObject;

public abstract class Weapon extends Component implements ObjectStorage{
    ObjectStorage weaponStorage;

    public void fire()
    {
        
    }

    @Override
    public void store(GameObject object) {

    }

    @Override
    public GameObject request() {
        return null;
    }
}
