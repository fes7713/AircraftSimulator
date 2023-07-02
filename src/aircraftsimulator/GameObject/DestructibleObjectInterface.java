package aircraftsimulator.GameObject;

public interface DestructibleObjectInterface extends GameObjectInterface {
    float getHealth();
    void takeDamage(float damage);
    void destroyed();
    boolean isAlive();
}
