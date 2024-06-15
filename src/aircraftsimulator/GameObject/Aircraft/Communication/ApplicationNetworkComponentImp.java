package aircraftsimulator.GameObject.Aircraft.Communication;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ApplicationNetworkComponentImp extends NetworkComponentImp implements ApplicationNetworkComponent {

    private Map<String, ResendPacket> sentPacketMap;
    private final Map<String, Integer> resentNumberMap;
    private final Map<String, Packet> lastSentPacketMap;

    private final int resentThreshold;
    private final long timeout;

    private static final long DEFAULT_TIMEOUT = 3000;
    private static final int DEFAULT_RESENT_THRESHOLD = 3;

    public ApplicationNetworkComponentImp(Network network, float updateInterval) {
        this(network, updateInterval, DEFAULT_TIMEOUT, DEFAULT_RESENT_THRESHOLD);
    }

    public ApplicationNetworkComponentImp(Network network, float updateInterval, long timeout, int resentThreshold)
    {
        super(network, updateInterval);
        resentNumberMap = new HashMap<>();
        lastSentPacketMap = new HashMap<>();

        this.timeout = timeout;
        this.resentThreshold = resentThreshold;
    }

    private void resendData(String sessionId)
    {
        if(!sessionManager.isTimeout(sessionId, timeout))
            resentNumberMap.remove(sessionId);

        if(!sessionManager.isTimeout(sessionId, timeout * (resentNumberMap.getOrDefault(sessionId, 0) + 1)))
            return;
        else
            resentNumberMap.put(sessionId, resentNumberMap.getOrDefault(sessionId, 0) + 1);

        SessionInformation info = sessionManager.getSessionInformation(sessionId);

        if(resentNumberMap.get(sessionId) > resentThreshold)
        {
            int port = sessionManager.getSessionInformation(sessionId).sourcePort();
            releasePort(port);
            resentNumberMap.remove(sessionId);
            return;
        }

        if(lastSentPacketMap.containsKey(sessionId))
        {
            send(lastSentPacketMap.get(sessionId));
            System.out.printf("[%6s-%6s] Port [%d] Resent last packet\n", getMac().substring(0, 6), info.destinationMac().substring(0, 6), info.sourcePort());
        }
        else{
            System.out.printf("[%6s-%6s] Port [%d] Failed to resent last packet\n", getMac().substring(0, 6), info.destinationMac().substring(0, 6), info.sourcePort());
        }
    }

    @Override
    public void send(Packet packet) {
        super.send(packet);
        lastSentPacketMap.put(packet.getSessionID(), packet);
    }

    public void setResendLimit(Integer limit) {

    }

    public void send(){

    }

    public void received(){

    }

    @Override
    public void triggerTimeout(Integer port, SessionInformation sessionInformation) {
        if(portStateMap.get(port) == PortState.CONNECTING) {
//            changePortState(port, PortState.OPEN, sessionManager.getSessionId(port), sessionInformation.destinationPort(), sessionInformation.destinationMac());
//            sessionManager.updateSession(sessionManager.getSessionId(port));
            resendData(sessionManager.getSessionId(port));
        }
    }

    private class ResendPacket
    {
        private int remainingResendLimit;
        private int port;
        private int sequenceNumber;
        private int ackNumber;
        private int windowSize;
        private Serializable data;

        public void decrementResendLimit()
        {

        }

        public boolean isActive(){
            return remainingResendLimit > 0;
        }
    }
}
