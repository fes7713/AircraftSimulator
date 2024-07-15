package aircraftsimulator.GameObject.Aircraft.Communication;

import aircraftsimulator.GameObject.Aircraft.Communication.Data.Data;
import aircraftsimulator.GameObject.Aircraft.Communication.Data.EmptyData;
import aircraftsimulator.GameObject.Aircraft.Communication.Data.FragmentedData;
import aircraftsimulator.GameObject.Aircraft.Communication.Data.KeepAliveData;
import aircraftsimulator.GameObject.Aircraft.Communication.Handler.*;
import aircraftsimulator.GameObject.Aircraft.Communication.Handler.NetworkError.DefaultNetworkErrorHandler;
import aircraftsimulator.GameObject.Aircraft.Communication.Handler.NetworkError.NetworkErrorHandler;
import aircraftsimulator.GameObject.Aircraft.Communication.Handler.NetworkError.NetworkErrorType;
import aircraftsimulator.GameObject.Aircraft.Communication.Handler.SendCompletion.DefaultSendCompletionHandler;
import aircraftsimulator.GameObject.Aircraft.Communication.Handler.SendCompletion.SendCompletionHandler;
import aircraftsimulator.GameObject.Aircraft.Communication.Logger.Logger;
import aircraftsimulator.GameObject.Aircraft.Communication.Logger.ProgressLogger;
import aircraftsimulator.GameObject.Aircraft.Communication.Timeout.TimeoutInformation;

import java.io.Serializable;
import java.util.Queue;

public class SlowStartApplicationNetworkComponentImp extends NetworkComponentImp implements ApplicationNetworkComponent, KeepAliveHandler, FragmentAdaptor {
    private final FragmentHandler fragmentHandler;
    private final SendCompletionHandler sendCompletionHandler;

    private final long keepaliveTime;
    private final long keepAliveInterval;
    private final int keepAliveRetry;

    private final int windowSize;

    private static final long DEFAULT_TIMEOUT = 3000;
    private static final int DEFAULT_RESENT_RETRY= 3;

    private static final long DEFAULT_KEEP_ALIVE_TIME = 10000;
    private static final long DEFAULT_KEEP_ALIVE_INTERVAL = 3000;
    private static final int DEFAULT_KEEP_ALIVE_RETRY = 3;

    private static final int DEFAULT_WINDOW_SIZE = 20;

    public SlowStartApplicationNetworkComponentImp(Network network)
    {
        this(network, DEFAULT_UPDATE_INTERVAL);
    }

    public SlowStartApplicationNetworkComponentImp(Network network, float updateInterval) {
        this(network, updateInterval, new DefaultSendCompletionHandler(), new DefaultNetworkErrorHandler());
    }

    public SlowStartApplicationNetworkComponentImp(Network network, float updateInterval, SendCompletionHandler sendCompletionHandler, NetworkErrorHandler errorHandler) {
        this(network, updateInterval, DEFAULT_TIMEOUT, DEFAULT_RESENT_RETRY, DEFAULT_KEEP_ALIVE_TIME, DEFAULT_KEEP_ALIVE_INTERVAL, DEFAULT_KEEP_ALIVE_RETRY, sendCompletionHandler, errorHandler);
    }

    public SlowStartApplicationNetworkComponentImp(Network network, float updateInterval, long resendTimeout, int resentRetry, long keepaliveTime, long keepAliveInterval, int keepAliveRetry, SendCompletionHandler sendCompletionHandler, NetworkErrorHandler errorHandler)
    {
        super(network, updateInterval, errorHandler);
        this.sendCompletionHandler = sendCompletionHandler;

        this.keepaliveTime = keepaliveTime;
        this.keepAliveInterval = keepAliveInterval;
        this.keepAliveRetry = keepAliveRetry;

        this.windowSize = DEFAULT_WINDOW_SIZE;

        addDataReceiver(KeepAliveData.class, (object, port) -> {
            sendData(port, new EmptyData());
        });

        // TODO Better init system
        fragmentHandler = new ContinuousFragmentHandler(resendTimeout, timeoutManager, this);
        network.addToNetwork(this);
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
            if(fragmentHandler.isIdle(port))
                fragmentHandler.sendData(port, stream);
            ProgressLogger.AddProgressData(sessionId, stream.length);
            System.out.println();
        }else{
            send(packet.copy(ByteConvertor.serialize(data)));
        }
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
    public void sendCompletionHandler(String sessionId) {
        sendCompletionHandler.handle(sessionManager.getPort(sessionId));
    }

    @Override
    public String getSessionId(int port) {
        return sessionManager.getSessionId(port);
    }

    @Override
    public void registerKeepAliveTimeout(String sessionId, long timeout) {

    }

    @Override
    public void handleKeepAlive(String sessionId, Integer retryNum) {
        Logger.Log(Logger.LogLevel.INFO, String.format("KEEP ALIVE [%d]", retryNum),
                getMac(), sessionManager.getSessionInformation(sessionId).destinationMac(), sessionManager.getPort(sessionId));
        sendData(sessionManager.getPort(sessionId), new KeepAliveData());
    }
}
