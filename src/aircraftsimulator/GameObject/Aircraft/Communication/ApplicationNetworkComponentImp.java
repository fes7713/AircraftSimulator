package aircraftsimulator.GameObject.Aircraft.Communication;

import aircraftsimulator.GameObject.Aircraft.Communication.Data.AckWindowSizeData;
import aircraftsimulator.GameObject.Aircraft.Communication.Data.FragmentedData;
import aircraftsimulator.GameObject.Aircraft.Communication.Data.KeepAliveData;
import aircraftsimulator.GameObject.Aircraft.Communication.Data.RequestWindowSize;
import aircraftsimulator.GameObject.Aircraft.Communication.Handler.FragmentHandler;
import aircraftsimulator.GameObject.Aircraft.Communication.Handler.KeepAliveAckHandler;
import aircraftsimulator.GameObject.Aircraft.Communication.Handler.KeepAliveHandler;
import aircraftsimulator.GameObject.Aircraft.Communication.Timeout.TimeoutInformation;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

public class ApplicationNetworkComponentImp extends NetworkComponentImp implements ApplicationNetworkComponent, KeepAliveHandler, KeepAliveAckHandler {
    private final Map<String, byte[][]> fragmentStoreMap;
    private final Map<String, Integer> fragmentLastSentMap;
    private final Map<String, Integer> windowSizeMap;

    private final int resendRetry;
    private final long resendTimeout;

    private final long keepaliveTime;
    private final long keepAliveInterval;
    private final int keepAliveRetry;

    private final int windowSize;

    private static final long DEFAULT_TIMEOUT = 3000;
    private static final int DEFAULT_RESENT_RETRY= 3;

    private static final long DEFAULT_KEEP_ALIVE_TIME = 1000000;
    private static final long DEFAULT_KEEP_ALIVE_INTERVAL = 3000;
    private static final int DEFAULT_KEEP_ALIVE_RETRY = 3;

    private static final int DEFAULT_WINDOW_SIZE = 5;

    public ApplicationNetworkComponentImp(Network network, float updateInterval) {
        this(network, updateInterval, DEFAULT_TIMEOUT, DEFAULT_RESENT_RETRY, DEFAULT_KEEP_ALIVE_TIME, DEFAULT_KEEP_ALIVE_INTERVAL, DEFAULT_KEEP_ALIVE_RETRY);
    }

    public ApplicationNetworkComponentImp(Network network, float updateInterval, long resendTimeout, int resentRetry, long keepaliveTime, long keepAliveInterval, int keepAliveRetry)
    {
        super(network, updateInterval);
        fragmentStoreMap = new HashMap<>();
        fragmentLastSentMap = new HashMap<>();
        windowSizeMap = new HashMap<>();

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

        addDataReceiver(FragmentedData.class, (data, sessionId) -> {
            int waitingFragment = 0;
            byte[][] fragmentArr = fragmentStoreMap.get(sessionId);
            for(int i = 0; i < fragmentArr.length; i++)
                if(fragmentArr[i] == null)
                {
                    waitingFragment = i;
                    break;
                }
            if(waitingFragment <= data.sequenceNumber())
            {
                if(waitingFragment == data.sequenceNumber())
                {
                    fragmentArr[waitingFragment] = data.fragmentedData();
                    StringBuilder sb = new StringBuilder();
                    for(int i = 0; i < fragmentArr.length; i++)
                        sb.append(fragmentArr[i] != null ? "[v]":"[ ]");
                    System.out.println(sb);
                }
                serializableDataSend(sessionId, new AckWindowSizeData(waitingFragment, askForWindowSize()));
            }

            if(waitingFragment == fragmentArr.length - 1) {
                try {
                    fragmentReceiveCompletionHandler(ByteConvertor.deSerialize(fragmentArr), sessionId);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });

        addDataReceiver(RequestWindowSize.class, ((data, sessionId) -> {
            fragmentStoreMap.put(sessionId, new byte[data.totalFrameSize()][]);
            serializableDataSend(sessionId, new AckWindowSizeData(0, askForWindowSize()));
        }));

        addDataReceiver(AckWindowSizeData.class, ((data, sessionId) -> {
            int lastSent = fragmentLastSentMap.getOrDefault(sessionId, -1);
            byte[][] fragmentArr = fragmentStoreMap.get(sessionId);
            if(fragmentArr[data.ackNumber()] == null)
                return;
            if(data.ackNumber() == fragmentArr.length - 1)
            {
                fragmentSendCompletionHandler(sessionId);
                return;
            }

            for(int i = 0; i < data.ackNumber(); i++)
                    fragmentArr[i] = null;
            boolean sentFlag = false;
            for(int i = lastSent + 1; i < data.windowSize() + data.ackNumber() && i < fragmentArr.length; i++)
            {
                fragmentLastSentMap.put(sessionId, i);
                serializableDataSend(sessionId, new FragmentedData(fragmentArr[i], i, 0, data.windowSize(), fragmentArr.length));
                sentFlag = true;
            }
            if(sentFlag)
            {
                timeoutManager.registerTimeout(sessionId, FragmentHandler.class,
                        new TimeoutInformation(sessionId, resendTimeout, resendTimeout, 0, 1, 3,
                                (s, integer) -> {
                                    for(int i = data.ackNumber(); i < data.windowSize() + data.ackNumber() && i < fragmentArr.length; i++)
                                        serializableDataSend(s, new FragmentedData(fragmentArr[i], i, 0, data.windowSize(), fragmentArr.length));
                                },
                                s -> {
                                    timeoutManager.removeTimeout(s, FragmentHandler.class);
                                    fragmentStoreMap.remove(s);
                                }
                        ));
            }
        }));
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
            timeoutManager.registerTimeout(sessionId, FragmentHandler.class,
                    new TimeoutInformation(sessionId, resendTimeout, resendTimeout, 0, 1, 3,
                        ((s, integer) -> {
                            send(packet.copy(ByteConvertor.serialize(new RequestWindowSize(stream.length))));
                            }
                        ),
                        s -> {
                            timeoutManager.removeTimeout(s, FragmentHandler.class);
                            fragmentStoreMap.remove(s);
                        }
                    ));
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

    public void fragmentReceiveCompletionHandler(Object object, String sessionId) {
        fragmentStoreMap.remove(sessionId);
        triggerReceiver(sessionId, object);
    }

    public void fragmentSendCompletionHandler(String sessionId) {
        fragmentStoreMap.remove(sessionId);
        timeoutManager.removeTimeout(sessionId, FragmentHandler.class);
    }

    public void serializableDataSend(String sessionId, Serializable data) {
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
