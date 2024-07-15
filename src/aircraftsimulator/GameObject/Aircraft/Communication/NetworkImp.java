package aircraftsimulator.GameObject.Aircraft.Communication;

import java.util.*;

public class NetworkImp implements Network{
    private final Map<String, NetworkComponent> arpNetworkComponentMap;
    private final int frameSize;
    private final Queue<Packet> packetQueue;
    private final int queueSize;

    private final float updateInterval;
    private float timeClock;
    private final int networkSpeed;

    private final static int DEFAULT_NETWORK_SPEED = 4;
    private final static int DEFAULT_QUEUE_SIZE = 100;
    private final static int DEFAULT_FRAME_SIZE = 1024;

    public NetworkImp(float updateInterval) {
        this.arpNetworkComponentMap = new HashMap<>();
        packetQueue = new ArrayDeque<>();

        this.updateInterval = updateInterval;
        timeClock = updateInterval;

        networkSpeed = DEFAULT_NETWORK_SPEED;
        frameSize = DEFAULT_FRAME_SIZE;
        queueSize = DEFAULT_QUEUE_SIZE;
    }

    @Override
    public void addToNetwork(NetworkComponent networkComponent) {
        arpNetworkComponentMap.put(networkComponent.getMac(), networkComponent);
    }

    @Override
    public void removeFromNetwork(NetworkComponent networkComponent) {
        arpNetworkComponentMap.remove(networkComponent.getMac());
    }

    private void broadcast(Packet packet) {
        for(NetworkComponent networkComponent: arpNetworkComponentMap.values())
            if(!networkComponent.getMac().equals(packet.getSourceMac()))
                sendTo(packet.copy(packet.getSessionID(), networkComponent.getMac()));
    }

    @Override
    public void sendTo(Packet packet) {
        if(packet.getDestinationMac() == null)
            broadcast(packet);
        else{
            if(packetQueue.size() + 1 < queueSize)
                packetQueue.offer(packet);
//            while(packetQueue.size() > networkSpeed)
//                packetQueue.remove(new Random().nextInt(packetQueue.size()));
        }
    }

    @Override
    public int getFrameSize() {
        return frameSize;
    }

    @Override
    public void update(float delta) {
        if(timeClock < 0)
        {
            for(int i = 0; i < networkSpeed && !packetQueue.isEmpty(); i++)
            {
                Packet packet = packetQueue.poll();
                arpNetworkComponentMap.get(packet.getDestinationMac()).receive(packet);
            }
            timeClock = updateInterval;
        }
        else{
            timeClock -= delta;
        }
        for(NetworkComponent component: arpNetworkComponentMap.values())
            component.update(delta);
    }
}
