package aircraftsimulator.GameObject.Aircraft.Communication.NetwrokAdaptor;

import aircraftsimulator.GameObject.Aircraft.Communication.Event.Event;
import aircraftsimulator.GameObject.Aircraft.Communication.Event.Request.PingEvent;
import aircraftsimulator.GameObject.Aircraft.Communication.Event.Request.RequestEvent;
import aircraftsimulator.GameObject.Aircraft.Communication.Event.Response.DefaultResponseEvent;
import aircraftsimulator.GameObject.Aircraft.Communication.Event.Response.PingResponseEvent;
import aircraftsimulator.GameObject.Aircraft.Communication.Event.Response.ResponseEvent;

public class ResponsiveNetworkInterface extends DefaultNetworkInterface {
    private final DataProcessor processor;

    private NetworkInterfaceMode mode;

    public ResponsiveNetworkInterface(DataProcessor processor){
        this(processor, DEFAULT_PROCESS_TIME);
    }

    public ResponsiveNetworkInterface(DataProcessor processor, float processTime){
        super(processTime);
        this.processor = processor;
        mode = NetworkInterfaceMode.IDLE;
    }

    @Override
    public boolean update(float delta) {
        if(super.update(delta))
        {
            updateSwitchMode();
            return true;
        }
        return false;
    }

    private void updateSwitchMode()
    {
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
                        System.out.println("Ping : " + (System.currentTimeMillis() - p.getData()) + "ms [" + p.getSourceMac() + "]");
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
    }

    private <E> void sendEvent(Event<E> event)
    {
        if(router == null)
            return;
        sendingQueue.offer(event);
    }

    private void modeSwitch(NetworkInterfaceMode mode)
    {
        if(this.mode == NetworkInterfaceMode.IDLE)
            this.mode = mode;

        if(mode == NetworkInterfaceMode.IDLE)
            this.mode = mode;
    }
}
