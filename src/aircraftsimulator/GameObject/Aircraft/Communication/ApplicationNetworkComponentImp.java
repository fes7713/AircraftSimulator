package aircraftsimulator.GameObject.Aircraft.Communication;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ApplicationNetworkComponentImp extends NetworkComponentImp implements ApplicationNetworkComponent {

    private Map<String, ResendPacket> sentPacketMap;
    private final Map<String, Integer> timeoutNumberMap;
    private final Map<String, Integer> resentNumberMap;

    private final int resentThreshold;
    private final int timeoutThreshold;

    private static final int DEFAULT_TIMEOUT_THRESHOLD = 3;
    private static final int DEFAULT_RESENT_THRESHOLD = 3;

    public ApplicationNetworkComponentImp(Network network, float updateInterval) {
        this(network, updateInterval, DEFAULT_TIMEOUT_THRESHOLD, DEFAULT_RESENT_THRESHOLD);
    }

    public ApplicationNetworkComponentImp(Network network, float updateInterval, int timeoutThreshold, int resentThreshold)
    {
        super(network, updateInterval);
        timeoutNumberMap = new HashMap<>();
        resentNumberMap = new HashMap<>();

        this.timeoutThreshold = timeoutThreshold;
        this.resentThreshold = resentThreshold;
    }

    private void resendData(String sessionId)
    {
        if(!timeoutNumberMap.containsKey(sessionId))
        {
            resentNumberMap.put(sessionId, 0);
            timeoutNumberMap.put(sessionId, 0);
        }
        timeoutNumberMap.put(sessionId, timeoutNumberMap.getOrDefault(sessionId, 0) + 1);
        if(timeoutNumberMap.get(sessionId) < timeoutThreshold)
            return;

        timeoutNumberMap.put(sessionId, 0);
        System.out.println("Resending");
        resentNumberMap.put(sessionId, resentNumberMap.getOrDefault(sessionId, 0) + 1);
        if(resentNumberMap.get(sessionId) >= resentThreshold)
        {
            int port = sessionManager.getSessionInformation(sessionId).sourcePort();
            releasePort(port);
            resentNumberMap.remove(sessionId);
            timeoutNumberMap.remove(sessionId);
        }
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
            System.out.printf("[%6s-] Port [%d] timeout\n", getMac().substring(0, 6), port);
            sessionManager.updateSession(sessionManager.getSessionId(port));
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
