package aircraftsimulator.GameObject.Aircraft.Communication;

import aircraftsimulator.GameObject.Aircraft.Communication.Data.Data;
import aircraftsimulator.GameObject.Aircraft.Communication.Data.FragmentedData;
import aircraftsimulator.GameObject.Aircraft.Communication.Data.KeepAliveData;
import aircraftsimulator.GameObject.Aircraft.Communication.Handler.*;
import aircraftsimulator.GameObject.Aircraft.Communication.NetworkError.DefaultNetworkErrorHandler;
import aircraftsimulator.GameObject.Aircraft.Communication.NetworkError.NetworkErrorHandler;
import aircraftsimulator.GameObject.Aircraft.Communication.NetworkError.NetworkErrorType;
import aircraftsimulator.GameObject.Aircraft.Communication.Timeout.TimeoutInformation;

import java.io.Serializable;
import java.util.Queue;

public class SlowStartApplicationNetworkComponentImp extends NetworkComponentImp implements ApplicationNetworkComponent, KeepAliveHandler, KeepAliveAckHandler, FragmentAdaptor {
    private final FragmentHandler fragmentHandler;

    private final long keepaliveTime;
    private final long keepAliveInterval;
    private final int keepAliveRetry;

    private final int windowSize;

    private static final long DEFAULT_TIMEOUT = 3000;
    private static final int DEFAULT_RESENT_RETRY= 3;

    private static final long DEFAULT_KEEP_ALIVE_TIME = 1000000;
    private static final long DEFAULT_KEEP_ALIVE_INTERVAL = 3000;
    private static final int DEFAULT_KEEP_ALIVE_RETRY = 3;

    private static final int DEFAULT_WINDOW_SIZE = 20;

    public SlowStartApplicationNetworkComponentImp(Network network, float updateInterval) {
        this(network, updateInterval, new DefaultNetworkErrorHandler());
    }

    public SlowStartApplicationNetworkComponentImp(Network network, float updateInterval, NetworkErrorHandler errorHandler) {
        this(network, updateInterval, DEFAULT_TIMEOUT, DEFAULT_RESENT_RETRY, DEFAULT_KEEP_ALIVE_TIME, DEFAULT_KEEP_ALIVE_INTERVAL, DEFAULT_KEEP_ALIVE_RETRY, errorHandler);
    }

    public SlowStartApplicationNetworkComponentImp(Network network, float updateInterval, long resendTimeout, int resentRetry, long keepaliveTime, long keepAliveInterval, int keepAliveRetry, NetworkErrorHandler errorHandler)
    {
        super(network, updateInterval, errorHandler);

        this.keepaliveTime = keepaliveTime;
        this.keepAliveInterval = keepAliveInterval;
        this.keepAliveRetry = keepAliveRetry;

        this.windowSize = DEFAULT_WINDOW_SIZE;

        fragmentHandler = new ContinuousFragmentHandler(resendTimeout, timeoutManager, this);
        fragmentHandler.initHandler();
    }

    @Override
    public void sendData(Integer port, Serializable data) {
        if(portStateMap.getOrDefault(port, PortState.CLOSED) != PortState.CONNECTED)
        {
            errorHandler(port, NetworkErrorType.NOT_CONNECTED);
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
            fragmentHandler.sendData(sessionId, stream);
        }else{
            send(packet.copy(ByteConvertor.serialize(data)));
        }
    }

    @Override
    public void send(Packet packet) {
        super.send(packet);
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
    public <E extends Data> void serializableDataSend(String sessionId, E data) {
        send(new Packet(
                sessionId,
                sessionManager.getSessionInformation(sessionId),
                HandshakeData.ACK,
                ByteConvertor.serialize(data),
                getMac()
        ));
    }

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
