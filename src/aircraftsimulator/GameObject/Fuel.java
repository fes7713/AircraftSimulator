package aircraftsimulator.GameObject;

public class Fuel extends Explosive{
    private float efficiency; // energy per unit fuel
    private float amount;

    public Fuel(float efficiency, float amount) {
        this.efficiency = efficiency;
        this.amount = amount;
    }


    public boolean isEmpty()
    {
        return amount <= 0;
    }
}
