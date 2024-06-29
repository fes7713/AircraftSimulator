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

public class SlowStartApplicationNetworkComponentImp extends NetworkComponentImp implements ApplicationNetworkComponent, KeepAliveHandler, KeepAliveAckHandler {
    private final Map<String, byte[][]> fragmentStoreMap;
    private final Map<String, Integer> fragmentLastSentMap;
    private final Map<String, Integer> windowSizeMap;
    private final Map<String, Integer> progressMap;
    private final Map<String, Integer> windowChangePointMap;

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

    private static final int DEFAULT_WINDOW_SIZE = 20;

    public SlowStartApplicationNetworkComponentImp(Network network, float updateInterval) {
        this(network, updateInterval, DEFAULT_TIMEOUT, DEFAULT_RESENT_RETRY, DEFAULT_KEEP_ALIVE_TIME, DEFAULT_KEEP_ALIVE_INTERVAL, DEFAULT_KEEP_ALIVE_RETRY);
    }

    public SlowStartApplicationNetworkComponentImp(Network network, float updateInterval, long resendTimeout, int resentRetry, long keepaliveTime, long keepAliveInterval, int keepAliveRetry)
    {
        super(network, updateInterval);
        fragmentStoreMap = new HashMap<>();
        fragmentLastSentMap = new HashMap<>();
        windowSizeMap = new HashMap<>();
        progressMap = new HashMap<>();
        windowChangePointMap = new HashMap<>();

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
            int waitingFragment = progressMap.get(sessionId);
            byte[][] fragmentArr = fragmentStoreMap.get(sessionId);

            if(waitingFragment > data.sequenceNumber())
                return;

            if(waitingFragment < data.sequenceNumber())
            {
                serializableDataSend(sessionId, new AckWindowSizeData(waitingFragment, askForWindowSize()));
                return;
            }

            fragmentArr[waitingFragment] = data.fragmentedData();
            StringBuilder sb = new StringBuilder();
            for(int i = 0; i < fragmentArr.length; i++)
                sb.append(fragmentArr[i] != null ? "[v]":"[ ]");
            System.out.println(sb);
            progressMap.put(sessionId, waitingFragment + 1);
            serializableDataSend(sessionId, new AckWindowSizeData(waitingFragment + 1, askForWindowSize()));
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
            progressMap.put(sessionId, 0);
        }));

        addDataReceiver(AckWindowSizeData.class, (data, sessionId) -> {
            if(fragmentStoreMap.get(sessionId) == null)
            {
                timeoutManager.removeTimeout(sessionId, FragmentHandler.class);
                return;
            }

            if(data.ackNumber() == 22)
                System.out.println("Here");

            int lastSent = fragmentLastSentMap.getOrDefault(sessionId, -1);
            int ackedTill = progressMap.get(sessionId);
            byte[][] fragmentArr = fragmentStoreMap.get(sessionId);

            if(ackedTill > data.ackNumber())
                return;

            if(lastSent + 1 < data.ackNumber())
                throw new RuntimeException("Illegal Ack");

            progressMap.put(sessionId, data.ackNumber() - 1);

            if(windowChangePointMap.get(sessionId) <= data.ackNumber())
            {
                windowSizeMap.put(sessionId, Math.min(windowSizeMap.get(sessionId) * 2, data.windowSize()));
                int windowsSize = windowSizeMap.get(sessionId);
                int lastIndex = Math.min(fragmentArr.length, lastSent + windowsSize + 1);
                windowChangePointMap.put(sessionId, lastIndex);
                fragmentLastSentMap.put(sessionId, lastIndex - 1);
                for(int i = lastSent + 1; i < lastIndex; i++)
                    serializableDataSend(sessionId, new FragmentedData(fragmentArr[i], i, 0, windowsSize, fragmentArr.length));
                if(fragmentArr.length == lastIndex && lastIndex != lastSent + 1)
                {
                    fragmentSendCompletionHandler(sessionId);
                    return;
                }
            }else{
                if(ackedTill  >= data.ackNumber() - 1)
                {
                    return;
                }
                else{
                    if(lastSent + 1 < fragmentArr.length)
                    {
                        fragmentLastSentMap.put(sessionId, lastSent + 1);
                        serializableDataSend(sessionId, new FragmentedData(fragmentArr[lastSent + 1], lastSent + 1, 0, windowSizeMap.get(sessionId), fragmentArr.length));
                    }else{
                        fragmentSendCompletionHandler(sessionId);
                        return;
                    }
                }
            }

            if(fragmentArr.length == data.ackNumber())
            {
                fragmentSendAckCompletionHandler(sessionId);
                return;
            }

            timeoutManager.registerTimeout(sessionId, FragmentHandler.class,
                    new TimeoutInformation(sessionId, resendTimeout, resendTimeout, 0, 1, 3,
                            (s, integer) -> {
                                int askedTill = progressMap.get(sessionId);
                                windowSizeMap.put(s, 1);
                                windowChangePointMap.put(s, askedTill + 1 + 1);
                                fragmentLastSentMap.put(s, askedTill + 1);
                                serializableDataSend(sessionId, new FragmentedData(fragmentArr[askedTill + 1], askedTill + 1, 0, 1, fragmentArr.length));
//                                for(int i = askedTill + 1; i <= last; i++)
//                                    serializableDataSend(sessionId, new FragmentedData(fragmentArr[i], i, 0, 1, fragmentArr.length));
                            },
                            s -> {
                                fragmentSendAckCompletionHandler(s);
                            }
                    )
            );
        });
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
            windowSizeMap.put(sessionId, 1);
            progressMap.put(sessionId, -1);
            fragmentLastSentMap.put(sessionId, -1);
            windowChangePointMap.put(sessionId, 0);

            send(packet.copy(ByteConvertor.serialize(new RequestWindowSize(stream.length))));
            timeoutManager.registerTimeout(sessionId, FragmentHandler.class,
                    new TimeoutInformation(sessionId, resendTimeout, resendTimeout, 0, 1, 3,
                        ((s, integer) -> {
                            send(packet.copy(ByteConvertor.serialize(new RequestWindowSize(stream.length))));
                            }
                        ),
                        s -> {
                            fragmentSendAckCompletionHandler(s);
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
        progressMap.remove(sessionId);
    }

    public void fragmentSendCompletionHandler(String sessionId) {
        System.out.println("Data Send Complete");
    }

    public void fragmentSendAckCompletionHandler(String sessionId)
    {
        fragmentStoreMap.remove(sessionId);
        timeoutManager.removeTimeout(sessionId, FragmentHandler.class);
        windowSizeMap.remove(sessionId);
        progressMap.remove(sessionId);
        fragmentLastSentMap.remove(sessionId);
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
