package aircraftsimulator.GameObject.Aircraft.Communication.NetwrokAdaptor;

import aircraftsimulator.GameObject.Aircraft.Communication.Event.BasicEvent;
import aircraftsimulator.GameObject.Aircraft.Communication.Event.Event;
import aircraftsimulator.GameObject.Aircraft.Communication.Event.EventPriority;
import aircraftsimulator.GameObject.Aircraft.Communication.Event.Request.PingEvent;
import aircraftsimulator.GameObject.Aircraft.Communication.Event.Request.RequestEvent;
import aircraftsimulator.GameObject.Aircraft.Communication.Event.Response.DefaultResponseEvent;
import aircraftsimulator.GameObject.Aircraft.Communication.Event.Response.PingResponseEvent;
import aircraftsimulator.GameObject.Aircraft.Communication.Event.Response.ResponseEvent;
import aircraftsimulator.GameObject.Aircraft.Communication.Router;

import java.util.Deque;
import java.util.concurrent.LinkedBlockingDeque;

public class DefaultNetworkInterface implements NetworkInterface{
    private final String mac;
    private Router router;
    private final DataProcessor processor;

    private NetworkInterfaceMode mode;
    private final Deque<Event> sendingQueue;
    private final Deque<Event> receivingQueue;
    private final float processTime;
    private float time;

    public final static float DEFAULT_PROCESS_TIME = 0.03F;

    public DefaultNetworkInterface(DataProcessor processor){
        this(processor, null);
    }

    public DefaultNetworkInterface(DataProcessor processor, Router router){
        this(processor, router, DEFAULT_PROCESS_TIME);
    }

    public DefaultNetworkInterface(DataProcessor processor, Router router, float processTime){
        this.mac = NetworkInterface.generateMAC();
        this.processor = processor;
        this.router = router;
        this.processTime = processTime;
        mode = NetworkInterfaceMode.IDLE;
        time = 0;
        sendingQueue = new LinkedBlockingDeque<>();
        receivingQueue = new LinkedBlockingDeque<>();
    }


    @Override
    public void update(float delta) {
        if(time - delta > 0)
            time -= delta;
        else
            time = 0;

        switch (mode) {
            case IDLE -> {
                if(receivingQueue.size() > sendingQueue.size())
                    modeSwitch(NetworkInterfaceMode.RECEIVING);
                else if(!sendingQueue.isEmpty())
                    modeSwitch(NetworkInterfaceMode.SENDING);
            }
            case SENDING -> {
                router.receiveData(sendingQueue.pop());
                if(sendingQueue.isEmpty())
                    modeSwitch(NetworkInterfaceMode.IDLE);
            }
            case RECEIVING -> {
                Event event = receivingQueue.pop();
                if(event instanceof RequestEvent)
                {
                    if(event instanceof PingEvent pingEvent)
                    {
                        sendEvent(new PingResponseEvent(pingEvent, router.askForPort(pingEvent.getSourceMac()), this));
                    }else{
                        boolean result = processor.process(event);
                        sendEvent(new DefaultResponseEvent(event, result));
                    }
                }
                else if(event instanceof ResponseEvent)
                {
                    if(event instanceof PingResponseEvent p)
                        System.out.println("Ping : " + (System.currentTimeMillis() - p.getData()));
                    else if(event instanceof DefaultResponseEvent)
                        System.out.println("Reply : " + event.getData().toString());
                    else
                        throw new RuntimeException("New response");
                }
                else{
                    throw new RuntimeException("Error response");
                }

                if(receivingQueue.isEmpty())
                    modeSwitch(NetworkInterfaceMode.IDLE);
            }
        }
        time = processTime;
    }

    private <E> void sendEvent(Event<E> event)
    {
        if(router == null)
            return;
        sendingQueue.offer(event);
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
    public String getMac() {
        return mac;
    }

    @Override
    public float getProcessTime() {
        return time;
    }

    @Override
    public NetworkInterfaceMode getNetworkMode() {
        return mode;
    }

    private void modeSwitch(NetworkInterfaceMode mode)
    {
        if(this.mode == NetworkInterfaceMode.IDLE)
            this.mode = mode;

        if(mode == NetworkInterfaceMode.IDLE)
            this.mode = mode;
    }
}
