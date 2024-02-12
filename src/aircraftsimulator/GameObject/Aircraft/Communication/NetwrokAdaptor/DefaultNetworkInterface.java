package aircraftsimulator.GameObject.Aircraft.Communication.NetwrokAdaptor;

import aircraftsimulator.GameObject.Aircraft.Communication.Event.BasicEvent;
import aircraftsimulator.GameObject.Aircraft.Communication.Event.Event;
import aircraftsimulator.GameObject.Aircraft.Communication.Event.EventPriority;
import aircraftsimulator.GameObject.Aircraft.Communication.Router;
import aircraftsimulator.GameObject.Aircraft.Communication.TimeScheduler.QuantumScheduler;
import aircraftsimulator.GameObject.Aircraft.Communication.TimeScheduler.TimeScheduler;

import java.util.Deque;
import java.util.concurrent.LinkedBlockingDeque;

public class DefaultNetworkInterface implements NetworkInterface{
    protected final String mac;
    protected final String name;
    protected Router router;

    protected final Deque<Event> sendingQueue;
    protected final Deque<Event> receivingQueue;

    protected TimeScheduler timeScheduler;

    public final static float DEFAULT_PROCESS_TIME = 0.00F;

    public DefaultNetworkInterface(){
        this("Default name", DEFAULT_PROCESS_TIME);
    }

    public DefaultNetworkInterface(String name)
    {
        this(name, DEFAULT_PROCESS_TIME);
    }

    public DefaultNetworkInterface(String name, float processTime){
        this.name = name;
        this.mac = NetworkInterface.generateMAC();
        this.timeScheduler = new QuantumScheduler(processTime);
        sendingQueue = new LinkedBlockingDeque<>();
        receivingQueue = new LinkedBlockingDeque<>();
    }


    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean update(float delta) {
        return timeScheduler.update(delta);
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
    public Event popData() {
        return receivingQueue.poll();
    }
}
