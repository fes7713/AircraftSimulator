package aircraftsimulator.GameObject.Aircraft.Spawner;

import aircraftsimulator.GameObject.Component.Component;
import aircraftsimulator.GameObject.GameObject;

import java.awt.*;

public class Spawner<T> extends Component implements SpawnerInterface<T> {

    @Override
    public void update(float delta) {

    }

    @Override
    public void draw(Graphics2D g2d) {

    }

    @Override
    public void setParent(GameObject parent) {

    }

    @Override
    public void spawn() {

    }

    @Override
    public T createObject() {
        return null;
    }

    @Override
    public boolean trigger() {
        return false;
    }
}
