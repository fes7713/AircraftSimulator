package aircraftsimulator.GameObject.Component;

import aircraftsimulator.GameObject.GameObject;

import java.awt.*;
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

    @Override
    public void update(float delta) {

    }

    @Override
    public void draw(Graphics2D g2d) {

    }

    @Override
    public void setParent(GameObject parent) {

    }
}

