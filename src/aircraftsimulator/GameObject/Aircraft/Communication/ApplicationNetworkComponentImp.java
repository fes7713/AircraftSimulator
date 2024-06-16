package aircraftsimulator.GameObject.Aircraft.Communication;

import aircraftsimulator.GameObject.Aircraft.Communication.Data.FragmentedData;
import aircraftsimulator.GameObject.Aircraft.Communication.Data.KeepAliveData;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

public class ApplicationNetworkComponentImp extends NetworkComponentImp implements ApplicationNetworkComponent, FragmentHandler {

    private final Map<String, Integer> resentNumberMap;
    private final Map<String, Packet> lastSentPacketMap;
    private final Map<String, byte[][]> fragmentStoreMap;

    private final int resendRetry;
    private final long resendTimeout;

    private final long keepaliveTime;
    private final long keepAliveInterval;
    private final int keepAliveRetry;

    private static final long DEFAULT_TIMEOUT = 3000;
    private static final int DEFAULT_RESENT_RETRY= 3;

    private static final long DEFAULT_KEEP_ALIVE_TIME = 10000;
    private static final long DEFAULT_KEEP_ALIVE_INTERVAL = 3000;
    private static final int DEFAULT_KEEP_ALIVE_RETRY = 3;

    public ApplicationNetworkComponentImp(Network network, float updateInterval) {
        this(network, updateInterval, DEFAULT_TIMEOUT, DEFAULT_RESENT_RETRY, DEFAULT_KEEP_ALIVE_TIME, DEFAULT_KEEP_ALIVE_INTERVAL, DEFAULT_KEEP_ALIVE_RETRY);
    }

    public ApplicationNetworkComponentImp(Network network, float updateInterval, long resendTimeout, int resentRetry, long keepaliveTime, long keepAliveInterval, int keepAliveRetry)
    {
        super(network, updateInterval);
        resentNumberMap = new HashMap<>();
        lastSentPacketMap = new HashMap<>();
        fragmentStoreMap = new HashMap<>();

        this.resendTimeout = resendTimeout;
        this.resendRetry = resentRetry;

        this.keepaliveTime = keepaliveTime;
        this.keepAliveInterval = keepAliveInterval;
        this.keepAliveRetry = keepAliveRetry;

        addDataReceiver(KeepAliveData.class, (object, sessionId) -> {
            send(new Packet(
                    sessionId,
                    sessionManager.getSessionInformation(sessionId),
                    HandshakeData.ACK,
                    null,
                    getMac()
            ));
        });

        addDataReceiver(FragmentedData.class, this);
    }

    private void resendData(String sessionId)
    {
        if(!lastSentPacketMap.containsKey(sessionId))
            return;
        if(!sessionManager.isTimeout(sessionId, resendTimeout))
            resentNumberMap.remove(sessionId);

        if(!sessionManager.isTimeout(sessionId, resendTimeout * (resentNumberMap.getOrDefault(sessionId, 0) + 1)))
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
            System.out.printf("[%6s-] Port [%d] Failed to resent last packet\n", getMac().substring(0, 6), info.sourcePort());
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
                new Packet(
                        sessionId,
                        info,
                        new HandshakeData(false, false, false, false),
                        ByteConvertor.serialize(new KeepAliveData()),
                        getMac()
                )
        );
        System.out.printf("[%6s-%6s] Port [%d] keep alive [%d]\n", getMac().substring(0, 6), info.destinationMac().substring(0, 6), info.sourcePort(), resentNumberMap.get(sessionId));
    }

    @Override
    public void sendData(Integer port, Serializable data) {
        if(portStateMap.getOrDefault(port, PortState.CLOSED) != PortState.CONNECTED)
        {
            System.out.printf("[%6s-%6s] Port [%d] Failed to send data [%s]\n", getMac().substring(0, 6), "", port, data.toString());
            return;
        }
        String sessionId = sessionManager.getSessionId(port);
        SessionInformation sessionInformation = sessionManager.getSessionInformation(sessionId);

        Packet packet = new Packet(sessionId, sessionInformation, HandshakeData.EMPTY, null, this.getMac());
        Queue<FragmentedData> fragmentedData = FragmentedData.fragmentPacket(data, network.getFrameSize() - FragmentedData.FIXED_PRE_SIZE);
        if(fragmentedData.size() > 1)
        {
            while(!fragmentedData.isEmpty())
            {
                System.out.printf("[%6s-%6s] Port [%d] Data Sent[%s]\n", getMac().substring(0, 6), "", sessionInformation.sourcePort(), data.toString());
                byte[] arr = ByteConvertor.serialize(fragmentedData.poll());
                System.out.println(arr.length);
                send(packet.copy(arr));
            }
        }else{
            send(packet.copy(ByteConvertor.serialize(data)));
        }
    }

    @Override
    public void send(Packet packet) {
        super.send(packet);
        if(packet.getDestinationMac() != null)
            lastSentPacketMap.put(packet.getSessionID(), packet);
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

    @Override
    public void startStoringFragments(FragmentedData data, String sessionId) {
        fragmentStoreMap.put(sessionId, new byte[data.totalFrames()][]);
    }

    @Override
    public byte @Nullable [][] getFragmentArr(String sessionId) {
        return fragmentStoreMap.getOrDefault(sessionId, null);
    }

    @Override
    public void fragmentCompletionHandler(Object object, String sessionId) {
        triggerReceiver(sessionId, object);
    }
}
