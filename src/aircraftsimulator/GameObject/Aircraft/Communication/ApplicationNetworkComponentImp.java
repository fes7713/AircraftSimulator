package aircraftsimulator.GameObject.Aircraft.Communication;

import aircraftsimulator.GameObject.Aircraft.Communication.Data.*;
import aircraftsimulator.GameObject.Aircraft.Communication.Handler.FragmentHandler;
import aircraftsimulator.GameObject.Aircraft.Communication.Handler.KeepAliveAckHandler;
import aircraftsimulator.GameObject.Aircraft.Communication.Handler.KeepAliveHandler;
import aircraftsimulator.GameObject.Aircraft.Communication.Timeout.TimeoutInformation;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

public class ApplicationNetworkComponentImp extends NetworkComponentImp implements ApplicationNetworkComponent, FragmentHandler, KeepAliveHandler, KeepAliveAckHandler {

    private final Map<String, Integer> resentNumberMap;
    private final Map<String, Packet> lastSentPacketMap;
    private final Map<String, byte[][]> fragmentStoreMap;

    private final int resendRetry;
    private final long resendTimeout;

    private final long keepaliveTime;
    private final long keepAliveInterval;
    private final int keepAliveRetry;

    private final int windowSize;

    private static final long DEFAULT_TIMEOUT = 3000;
    private static final int DEFAULT_RESENT_RETRY= 3;

    private static final long DEFAULT_KEEP_ALIVE_TIME = 10000;
    private static final long DEFAULT_KEEP_ALIVE_INTERVAL = 3000;
    private static final int DEFAULT_KEEP_ALIVE_RETRY = 3;

    private static final int DEFAULT_WINDOW_SIZE = 5;

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

        this.windowSize = DEFAULT_WINDOW_SIZE;

        addDataReceiver(KeepAliveData.class, (object, sessionId) -> {
            sendData(sessionManager.getSessionInformation(sessionId).sourcePort(), null);
        });

//        addDataReceiver(EmptyData.class, (object, sessionId) -> {
//            timeoutManager.registerTimeout(sessionId, KeepAliveHandler.class, new TimeoutInformation(sessionId, keepaliveTime, keepAliveInterval, 1, 0, keepAliveRetry, this::handleKeepAlive, timeoutManager::removeTimeout));
//        });

        addDataReceiver(FragmentedData.class, this::fragmentDataReceiver);

        addDataReceiver(RequestWindowSize.class, this::requestWindowSizeDataReceiver);

        addDataReceiver(AckWindowSizeData.class, this::ackWindowDataReceived);
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
            byte[][] stream = new byte[fragmentedData.size()][];
            for(int i = 0; !fragmentedData.isEmpty(); i++)
                stream[i] = fragmentedData.poll().fragmentedData();
            fragmentStoreMap.put(sessionId, stream);
            send(packet.copy(ByteConvertor.serialize(new RequestWindowSize(stream.length))));
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
    protected void processData(byte[] data, String sessionId, boolean ack) {
        timeoutManager.updateTimeout(sessionId, KeepAliveHandler.class);
        super.processData(data, sessionId, ack);
    }

    @Override
    public void handleConnectionEstablished(String sessionId, Integer port) {
        super.handleConnectionEstablished(sessionId, port);
        timeoutManager.registerTimeout(
                sessionId,
                KeepAliveHandler.class,
                new TimeoutInformation(sessionId, keepaliveTime, keepAliveInterval, 1, 1, keepAliveRetry, this::handleKeepAlive, s -> {releasePort(sessionManager.getSessionInformation(s).sourcePort());}));
    }

    @Override
    public void startStoringFragments(int totalFrames, String sessionId) {
        fragmentStoreMap.put(sessionId, new byte[totalFrames][]);
    }

    @Override
    public byte @Nullable [][] getFragmentArr(String sessionId) {
        return fragmentStoreMap.getOrDefault(sessionId, null);
    }

    @Override
    public void fragmentReceiveCompletionHandler(Object object, String sessionId) {
        fragmentStoreMap.remove(sessionId);
        triggerReceiver(sessionId, object);
    }

    @Override
    public void fragmentSendCompletionHandler(String sessionId) {
        fragmentStoreMap.remove(sessionId);
    }

    @Override
    public void serializableDataSend(String sessionId, Serializable data) {
        send(new Packet(
                sessionId,
                sessionManager.getSessionInformation(sessionId),
                HandshakeData.ACK,
                ByteConvertor.serialize(data),
                getMac()
        ));
    }

    @Override
    public int askForWindowSize() {
        return this.windowSize;
    }

    @Override
    public void registerKeepAliveTimeout(String sessionId, long timeout) {

    }

    @Override
    public void handleKeepAlive(String sessionId, Integer retryNum) {
        sendData(sessionManager.getSessionInformation(sessionId).sourcePort(), new KeepAliveData());
    }

    @Override
    public void handleKeepAliveAck(String sessionId, Integer retryNum) {
        timeoutManager.updateTimeout(sessionId, KeepAliveAckHandler.class);
        System.out.println(retryNum);
    }
}
