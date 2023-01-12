package aircraftsimulator.GameObject;

public interface DestructibleObjectInterface extends GameObjectInterface{
    float getHealth();
    boolean takeDamage(float damage);
}
