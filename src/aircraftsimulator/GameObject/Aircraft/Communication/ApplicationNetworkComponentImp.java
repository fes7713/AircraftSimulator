package aircraftsimulator.GameObject.Aircraft.Communication;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ApplicationNetworkComponentImp extends NetworkComponentImp implements ApplicationNetworkComponent {

    private Map<String, ResendPacket> sentPacketMap;
    private final Map<String, Integer> resentNumberMap;
    private final Map<String, Packet> lastSentPacketMap;

    private final int resendRetry;
    private final long timeout;

    private final long keepaliveTime;
    private final long keepAliveInterval;
    private final int keepAliveRetry;

    private static final long DEFAULT_TIMEOUT = 3000;
    private static final int DEFAULT_RESENT_RETRY= 3;

    private static final long DEFAULT_KEEP_ALIVE_TIME = 10000;
    private static final long DEFAULT_KEEP_ALIVE_INTERVAL = 3000;
    private static final int DEFAULT_KEEP_ALIVE_RETRY = 3;

    public ApplicationNetworkComponentImp(Network network, float updateInterval) {
        this(network, updateInterval, DEFAULT_TIMEOUT, DEFAULT_RESENT_RETRY);
    }

    public ApplicationNetworkComponentImp(Network network, float updateInterval, long timeout, int resentRetry)
    {
        super(network, updateInterval);
        resentNumberMap = new HashMap<>();
        lastSentPacketMap = new HashMap<>();

        this.timeout = timeout;
        this.resendRetry = resentRetry;

        this.keepaliveTime = DEFAULT_KEEP_ALIVE_TIME;
        this.keepAliveInterval = DEFAULT_KEEP_ALIVE_INTERVAL;
        this.keepAliveRetry = DEFAULT_KEEP_ALIVE_RETRY;
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

        if(resentNumberMap.get(sessionId) > resendRetry)
        {
            int port = sessionManager.getSessionInformation(sessionId).sourcePort();
            releasePort(port);
            resentNumberMap.remove(sessionId);
            return;
        }

        if(lastSentPacketMap.containsKey(sessionId))
        {
            send(lastSentPacketMap.get(sessionId));
            System.out.printf("[%6s-%6s] Port [%d] Resent last packet [%d]\n", getMac().substring(0, 6), info.destinationMac().substring(0, 6), info.sourcePort(), resentNumberMap.get(sessionId));
        }
        else{
            System.out.printf("[%6s-%6s] Port [%d] Failed to resent last packet\n", getMac().substring(0, 6), info.destinationMac().substring(0, 6), info.sourcePort());
        }
    }

    private void keepAlive(String sessionId)
    {
        if(!sessionManager.isTimeout(sessionId, keepaliveTime))
            resentNumberMap.remove(sessionId);

        if(!sessionManager.isTimeout(sessionId, keepaliveTime + keepAliveInterval * resentNumberMap.getOrDefault(sessionId, 0)))
            return;
        else
            resentNumberMap.put(sessionId, resentNumberMap.getOrDefault(sessionId, 0) + 1);

        SessionInformation info = sessionManager.getSessionInformation(sessionId);

        if(resentNumberMap.get(sessionId) > keepAliveRetry)
        {
            int port = sessionManager.getSessionInformation(sessionId).sourcePort();
            releasePort(port);
            resentNumberMap.remove(sessionId);
            return;
        }

        send(
                new Packet(sessionId,
                new HandshakeData(false, false, false, false),
                    null,
                        info.sourcePort(),
                        info.destinationPort(),
                        getMac(),
                        info.destinationMac()
                )
        );
        System.out.printf("[%6s-%6s] Port [%d] keep alive [%d]\n", getMac().substring(0, 6), info.destinationMac().substring(0, 6), info.sourcePort(), resentNumberMap.get(sessionId));
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
        else if(portStateMap.get(port) == PortState.CONNECTED) {
            keepAlive(sessionManager.getSessionId(port));
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
