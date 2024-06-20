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

    @Override
    public void broadcast(Packet packet, String sourceMac, SessionManager sessionManager) {
        String sessionId = sessionManager.generateSession(
                packet.getSourcePort(),
                packet.getDestinationPort(),
                packet.getDestinationMac()
        );
        if(sessionId == null)
            return;
        for(NetworkComponent networkComponent: arpNetworkComponentMap.values())
            if(!networkComponent.getMac().equals(sourceMac))
                sendTo(packet.copy(sessionId, networkComponent.getMac()));
    }

    @Override
    public void sendTo(Packet packet) {
        packetQueue.add(packet);
        while(packetQueue.size() > networkSpeed)
            packetQueue.remove(new Random().nextInt(packetQueue.size()));
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
