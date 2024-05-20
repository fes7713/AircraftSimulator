package aircraftsimulator.GameObject.Aircraft.Communication;

import java.util.HashMap;
import java.util.Map;

public class NetworkImp implements Network{
    private final Map<String, NetworkComponent> arpNetworkComponentMap;

    public NetworkImp() {
        this.arpNetworkComponentMap = new HashMap<>();
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
                networkComponent.receive(new Packet(packet, sessionId));
    }

    @Override
    public void sendTo(String mac, Packet packet) {
        arpNetworkComponentMap.get(mac).receive(packet);
    }
}
