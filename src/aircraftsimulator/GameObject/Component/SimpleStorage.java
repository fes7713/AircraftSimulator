package aircraftsimulator.GameObject.Component;

import aircraftsimulator.GameObject.GameObject;

import java.util.Stack;

public class SimpleStorage extends Component implements ObjectStorage{

    Stack<GameObject> storage;

    public SimpleStorage()
    {
        storage = new Stack<>();
    }

    @Override
    public void store(GameObject object) {
        storage.push(object);
    }

    @Override
    public GameObject request() {
        if(storage.isEmpty())
            return null;
        return storage.pop();
    }
}

