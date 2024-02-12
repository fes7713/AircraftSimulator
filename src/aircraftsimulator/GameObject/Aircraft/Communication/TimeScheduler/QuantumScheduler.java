package aircraftsimulator.GameObject.Aircraft.Communication.TimeScheduler;

public class QuantumScheduler implements TimeScheduler{
    protected final float processTime;
    protected float time;

    public final static float DEFAULT_PROCESS_TIME = 0.05F;

    public QuantumScheduler(){
        this(DEFAULT_PROCESS_TIME);
    }

    public QuantumScheduler(float processTime){
        this.processTime = processTime;
        time = 0;
    }

    @Override
    public boolean update(float delta) {
        if(time - delta > 0)
        {
            time -= delta;
            return false;
        }
        else
            time = processTime;

        return true;
    }

    @Override
    public float getProcessTime() {
        return time;
    }
}
