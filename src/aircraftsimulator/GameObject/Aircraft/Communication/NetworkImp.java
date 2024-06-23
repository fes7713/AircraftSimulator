package aircraftsimulator.GameObject.Aircraft.Communication;

import java.util.*;

public class NetworkImp implements Network{
    private final Map<String, NetworkComponent> arpNetworkComponentMap;
    private final int frameSize;
    private final List<Packet> packetQueue;
    private final int networkSpeed;

    private final static int DEFAULT_NETWORK_SPEED = 7;
    private final static int DEFAULT_FRAME_SIZE = 512;

    public NetworkImp() {
        this.arpNetworkComponentMap = new HashMap<>();
        packetQueue = new ArrayList<>();
        networkSpeed = DEFAULT_NETWORK_SPEED;
        frameSize = DEFAULT_FRAME_SIZE;
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
            packetQueue.add(packet);
            while(packetQueue.size() > networkSpeed)
                packetQueue.remove(new Random().nextInt(packetQueue.size()));
        }
    }

    @Override
    public int getFrameSize() {
        return frameSize;
    }

    @Override
    public void update(float delta) {
        for(Packet packet: packetQueue)
            arpNetworkComponentMap.get(packet.getDestinationMac()).receive(packet);
        packetQueue.clear();
    }
}
