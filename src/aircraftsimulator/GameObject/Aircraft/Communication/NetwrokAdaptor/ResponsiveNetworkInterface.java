package aircraftsimulator.GameObject.Aircraft.Communication.NetwrokAdaptor;

import aircraftsimulator.GameObject.Aircraft.Communication.Event.Event;
import aircraftsimulator.GameObject.Aircraft.Communication.Event.Request.RequestEvent;
import aircraftsimulator.GameObject.Aircraft.Communication.Event.Response.ResponseEvent;

public class ResponsiveNetworkInterface extends DefaultNetworkInterface {
    private final DataProcessor processor;

    private NetworkInterfaceMode mode;

    public ResponsiveNetworkInterface(DataProcessor processor){
        this("Default responsive", processor, DEFAULT_PROCESS_TIME);
    }

    public ResponsiveNetworkInterface(String name, DataProcessor processor){
        this(name, processor, DEFAULT_PROCESS_TIME);
    }

    public ResponsiveNetworkInterface(String name, DataProcessor processor, float processTime){
        super(name, processTime);
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
                router.dispatchEvent(sendingQueue.pop());
                if(sendingQueue.isEmpty())
                    modeSwitch(NetworkInterfaceMode.IDLE);
            }
            case RECEIVING -> {
                Event event = receivingQueue.pop();
                if(event instanceof RequestEvent) {
                    boolean result = processor.process(event);
                    System.out.printf("Event %b\n", result);
                }
                else if(event instanceof ResponseEvent)
                {
                    boolean result = processor.process(event);
                    System.out.printf("Event %b\n", result);
                }
                else{
                    throw new RuntimeException("Error response");
                }

                if(receivingQueue.isEmpty())
                    modeSwitch(NetworkInterfaceMode.IDLE);
            }
        }
    }

    private void modeSwitch(NetworkInterfaceMode mode)
    {
        if(this.mode == NetworkInterfaceMode.IDLE)
            this.mode = mode;

        if(mode == NetworkInterfaceMode.IDLE)
            this.mode = mode;
    }
}
