package aircraftsimulator.GameObject.Aircraft.Communication.NetwrokAdaptor;

import aircraftsimulator.GameObject.Aircraft.Communication.Event.BasicEvent;
import aircraftsimulator.GameObject.Aircraft.Communication.Event.Event;
import aircraftsimulator.GameObject.Aircraft.Communication.Event.EventPriority;
import aircraftsimulator.GameObject.Aircraft.Communication.Router;

import java.util.Deque;
import java.util.concurrent.LinkedBlockingDeque;

public class DefaultNetworkInterface implements NetworkInterface{
    protected final String mac;
    protected Router router;

    protected final Deque<Event> sendingQueue;
    protected final Deque<Event> receivingQueue;
    protected final float processTime;
    protected float time;

    public final static float DEFAULT_PROCESS_TIME = 0.00F;

    public DefaultNetworkInterface(){
        this(DEFAULT_PROCESS_TIME);
    }

    public DefaultNetworkInterface(float processTime){
        this.mac = NetworkInterface.generateMAC();
        this.processTime = processTime;
        time = 0;
        sendingQueue = new LinkedBlockingDeque<>();
        receivingQueue = new LinkedBlockingDeque<>();
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
    public <E> void sendData(int port, E data, EventPriority priority) {
        if(router == null)
            return;

        Event<E> event = new BasicEvent<>(port, data, priority);
        sendingQueue.offer(event);
    }

    @Override
    public <E> void receiveData(Event<E> event) {
        // Receive
        receivingQueue.offer(event);
    }

    @Override
    public void setRouter(Router router) {
        this.router = router;
    }

    @Override
    public Router getRouter() {
        return router;
    }

    @Override
    public String getMac() {
        return mac;
    }

    @Override
    public float getProcessTime() {
        return time;
    }
}
