package aircraftsimulator.GameObject.Aircraft.Communication;

import aircraftsimulator.GameObject.Aircraft.Communication.Data.EmptyData;
import aircraftsimulator.GameObject.Aircraft.Communication.Handler.ConnectionEstablishedHandler;
import aircraftsimulator.GameObject.Aircraft.Communication.Handler.ConnectionHandler;
import aircraftsimulator.GameObject.Aircraft.Communication.Handler.Handler;
import aircraftsimulator.GameObject.Aircraft.Communication.Handler.NetworkError.NetworkErrorHandler;
import aircraftsimulator.GameObject.Aircraft.Communication.Handler.NetworkError.NetworkErrorType;
import aircraftsimulator.GameObject.Aircraft.Communication.Logger.Logger;
import aircraftsimulator.GameObject.Aircraft.Communication.Timeout.TimeoutInformation;
import aircraftsimulator.GameObject.Aircraft.Communication.Timeout.TimeoutManager;
import aircraftsimulator.GameObject.Aircraft.Communication.Timeout.TimeoutManagerImp;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;

// TODO -> Unreleased session in code.

public class NetworkComponentImp implements NetworkComponent, ConnectionHandler {
    protected final Network network;
    private final String mac;
    private final NetworkErrorHandler errorHandler;

    protected final Map<Integer, PortState> portStateMap;
    private final Map<String, Integer> arpTable;
    protected final SessionManager sessionManager;
    protected final TimeoutManager timeoutManager;

    protected final List<Integer> portTransferMap;

    private final Queue<Packet> sendingQueue;
    private final Queue<Packet> receivingQueue;
    private NetworkMode networkMode;

    private final float updateInterval;
    private float timeClock;
    private final int networkSpeed;

    private final Map<Class<? extends Serializable>, DataReceiver> dataReceiverMapper;
    private final Map<Integer, ConnectionEstablishedHandler> connectionEstablishedHandlerMap;

    private final long timeout;
    private static final long DEFAULT_TIMEOUT = 5000;
    private final static int DEFAULT_NETWORK_SPEED = 5;
    protected final static float DEFAULT_UPDATE_INTERVAL = 0.01F;
    protected final static float CONNECTION_TIMEOUT_RETRY = 5;

    public NetworkComponentImp(Network network, NetworkErrorHandler errorHandler)
    {
        this(network, DEFAULT_UPDATE_INTERVAL, errorHandler);
    }

    public NetworkComponentImp(Network network, float updateInterval, NetworkErrorHandler errorHandler)
    {
        this.network = network;
        this.errorHandler = errorHandler;
        mac = UUID.randomUUID().toString();
        portStateMap = new HashMap<>();
        arpTable = new HashMap<>();
        sessionManager = new SessionManagerImp();
        timeoutManager = new TimeoutManagerImp();
        connectionEstablishedHandlerMap = new HashMap<>();
        portTransferMap = new ArrayList<>();

        sendingQueue = new ArrayDeque<>();
        receivingQueue = new ArrayDeque<>();
        networkMode = NetworkMode.IDLE;

        this.updateInterval = updateInterval;
        timeClock = updateInterval;
        timeout = DEFAULT_TIMEOUT;
        networkSpeed = DEFAULT_NETWORK_SPEED;

        dataReceiverMapper = new HashMap<>();
    }

    @Override
    public String getMac() {
        return mac;
    }


    @Override
    public <E extends Serializable> void addDataReceiver(Class<E> cls, DataReceiver<E> dataReceiver) {
        dataReceiverMapper.put(cls, dataReceiver);
    }

    @Override
    public void errorHandler(String sessionId, NetworkErrorType type) {
        if(errorHandler != null)
            errorHandler.handle(sessionManager.getPort(sessionId), type);
        connectionEstablishedHandlerMap.remove(sessionId);
    }

    @Override
    public void errorHandler(int port, NetworkErrorType type) {
        if(errorHandler != null)
            errorHandler.handle(port, type);
    }

    @Override
    public void registerTimeout(int port, long timeout, Consumer<Integer> handler) {
        timeoutManager.registerTimeout(sessionManager.getSessionId(port), Handler.class, timeout, (s, integer) -> handler.accept(port));
    }

    @Override
    public void updateTimeout(int port, long timeout) {
        timeoutManager.updateTimeout(sessionManager.getSessionId(port), Handler.class, timeout);
    }

    @Override
    public void removeTimeout(int port) {
        timeoutManager.removeTimeout(sessionManager.getSessionId(port));
    }

    @Override
    public void registerTimeout(String key, long timeout, Consumer<String> handler) {
        timeoutManager.registerTimeout(key, Handler.class, timeout, (s, integer) -> handler.accept(key));
    }

    @Override
    public void registerTimeout(String key, TimeoutInformation information) {
        timeoutManager.registerTimeout(key, Handler.class, information);
    }

    @Override
    public void updateTimeout(String key, long timeout) {
        timeoutManager.updateTimeout(key, Handler.class, timeout);
    }

    @Override
    public void removeTimeout(String key) {
        timeoutManager.removeTimeout(key);
    }

    @Override
    public void update(float delta) {
        if(timeClock < 0)
        {
            timeoutManager.checkTimeout();
            process();
            timeClock = updateInterval;
        }
        else{
            timeClock -= delta;
        }
    }

    public boolean openPort(int port)
    {
        switch (portStateMap.getOrDefault(port, PortState.CLOSED))
        {

            case OPEN -> {
                Logger.Log(Logger.LogLevel.INFO, "already open", getMac(), port);
                return true;
            }
            case CONNECTED, CONNECTING -> {
                Logger.Log(Logger.LogLevel.INFO, "in use", getMac(), port);
                return false;
            }
            case CLOSED ->{
                Logger.Log(Logger.LogLevel.INFO, "opened", getMac(), port);
                changePortState(port, PortState.OPEN);
                return true;
            }
        }
        return false;
    }

    @Override
    public void enabledPortTransfer(int port) {
        if(portTransferMap.contains(port))
        {
            Logger.Log(Logger.LogLevel.INFO, "already enabled port transfer", getMac(), port);
            return;
        }
        portTransferMap.add(port);
        Logger.Log(Logger.LogLevel.INFO, "enabled port transfer", getMac(), port);
    }

    public void closePort(int port)
    {
        PortState portState = portStateMap.getOrDefault(port, null);

        switch (portState)
        {
            case OPEN -> {
                Logger.Log(Logger.LogLevel.INFO, "closed", getMac(), port);
            }
            case CONNECTED, CONNECTING -> {
                Logger.Log(Logger.LogLevel.INFO, "closing", getMac(), port);
//                disconnect(port);
            }
            default -> {
                Logger.Log(Logger.LogLevel.INFO, "not open", getMac(), port);
            }
        }
        changePortState(port, null);
    }

    private void process()
    {
        switch (networkMode)
        {
            case IDLE -> {
                if(sendingQueue.size() < receivingQueue.size())
                    networkMode = NetworkMode.RECEIVING;
                else if(!sendingQueue.isEmpty())
                    networkMode = NetworkMode.SENDING;
            }
            case SENDING -> {
                for(int i = 0 ; i < networkSpeed; i++)
                {
                    Packet sendingPacket = sendingQueue.poll();
                    if(sendingPacket == null)
                        break;
                    network.sendTo(sendingPacket);
                }

                if(sendingQueue.isEmpty())
                    networkMode = NetworkMode.IDLE;
            }
            case RECEIVING -> {
                Packet receivingPacket = receivingQueue.poll();
                if(receivingQueue.isEmpty())
                    networkMode = NetworkMode.IDLE;

                HandshakeData handshakeData = receivingPacket.getHandshake();
                if(receivingPacket.getSourceMac() == null)
                {
                    Logger.Log(Logger.LogLevel.ERROR, "connection rejected SYN", getMac(), receivingPacket.getSourceMac(), receivingPacket.getDestinationPort());
                    return;
                }

                Packet responsePacket = null;
                int code = (handshakeData.isSyn() ? 1000:0) + (handshakeData.isAck() ? 100:0) + (handshakeData.isRst() ? 10:0) + (handshakeData.isFin() ? 1:0);
                if(!portStateMap.containsKey(receivingPacket.getDestinationPort()))
                {
                    Logger.Log(Logger.LogLevel.ERROR, "is closed", getMac(), receivingPacket.getSourceMac(), receivingPacket.getDestinationPort());
                    responsePacket = new Packet(
                            receivingPacket.getSessionID(),
                            receivingPacket.getSessionInformation().reverse(receivingPacket.getSourceMac()),
                            HandshakeData.RST_ACK,
                            null,
                            null
                    );
                    send(responsePacket);
                    sessionManager.deleteSession(receivingPacket.getSessionID());
                    timeoutManager.removeTimeout(receivingPacket.getSessionID());
                    return;
                }

                Integer port = receivingPacket.getDestinationPort();
                if(portStateMap.get(port) != PortState.OPEN && !sessionManager.isRegistered(receivingPacket.getSessionID(), receivingPacket.getSourceMac()))
                {
                    if (portTransferMap.contains(port))
                    {
                        int transferringPort = port * 1000;
                        while(portStateMap.containsKey(transferringPort))
                            transferringPort++;
                        Logger.Log(Logger.LogLevel.INFO, "transferring to port [%d]".formatted(transferringPort), getMac(), receivingPacket.getSourceMac(), port);
                        openPort(transferringPort);
                        Logger.Log(Logger.LogLevel.INFO, "connecting SYN", getMac(), receivingPacket.getSourceMac(), transferringPort);
                        changePortState(receivingPacket.getSessionID(), new SessionInformation(transferringPort, receivingPacket.getSourcePort(), receivingPacket.getSourceMac()), PortState.CONNECTING);
                        send(new Packet(
                                receivingPacket.getSessionID(),
                                sessionManager.getSessionInformation(receivingPacket.getSessionID()),
                                HandshakeData.SYN_ACK,
                                null,
                                getMac()
                        ));
                        return;
                    }
                    Logger.Log(Logger.LogLevel.ERROR, 
                            String.format("Session id [%6s] is not registered", receivingPacket.getSessionID()), getMac(), receivingPacket.getSourceMac(), receivingPacket.getDestinationPort());
                    return;
                }

                String sessionId = sessionManager.getSessionId(port);
                if(sessionId != null)
                    sessionManager.updateSession(sessionId);

                switch (code)
                {
                    // SYN
                    case 1000 -> {
                        if(portStateMap.get(port) == PortState.OPEN)
                        {
                            Logger.Log(Logger.LogLevel.INFO, "connecting SYN", getMac(), receivingPacket.getSourceMac(), receivingPacket.getDestinationPort());
                            changePortState(receivingPacket.getSessionID(), receivingPacket.getSessionInformation().reverse(receivingPacket.getSourceMac()), PortState.CONNECTING);
                            responsePacket = new Packet(
                                    receivingPacket.getSessionID(),
                                    sessionManager.getSessionInformation(receivingPacket.getSessionID()),
                                    HandshakeData.SYN_ACK,
                                    null,
                                    getMac()
                            );
                        }else{
                            Logger.Log(Logger.LogLevel.ERROR, "in use", getMac(), receivingPacket.getSourceMac(), receivingPacket.getDestinationPort());
                            sessionManager.deleteSession(receivingPacket.getSessionID());
                            timeoutManager.removeTimeout(receivingPacket.getSessionID());
                        }
                    }
                    // SYN ACK
                    case 1100 -> {
                        if(portStateMap.get(port) == PortState.CONNECTING)
                        {
                            Logger.Log(Logger.LogLevel.INFO, String.format("connected to [%6s] Port [%d]", receivingPacket.getSourceMac().substring(0, 6), receivingPacket.getSourcePort()), getMac(), receivingPacket.getSourceMac(), receivingPacket.getDestinationPort());
                            arpTable.put(receivingPacket.getSourceMac(), receivingPacket.getDestinationPort());
                            changePortState(receivingPacket.getSessionID(), receivingPacket.getSessionInformation().reverse(receivingPacket.getSourceMac()), PortState.CONNECTED);
                            send(new Packet(
                                    receivingPacket.getSessionID(),
                                    sessionManager.getSessionInformation(receivingPacket.getSessionID()),
                                    HandshakeData.ACK,
                                    null,
                                    getMac()
                            ));
                            handleConnectionEstablished(sessionId, port);
                        }else{
                            Logger.Log(Logger.LogLevel.ERROR, "invalid packet SYN ACK", getMac(), receivingPacket.getSourceMac(), receivingPacket.getDestinationPort());
                            responsePacket = new Packet(
                                    receivingPacket.getSessionID(),
                                    receivingPacket.getSessionInformation().reverse(receivingPacket.getSourceMac()),
                                    new HandshakeData(false, true, true, true),
                                    null,
                                    null
                            );
                            sessionManager.deleteSession(receivingPacket.getSessionID());
                            timeoutManager.removeTimeout(receivingPacket.getSessionID());
                        }
                    }
                    case 100 -> {
                        if(portStateMap.get(receivingPacket.getDestinationPort()) == PortState.CONNECTING){
                            Logger.Log(Logger.LogLevel.INFO,
                                    String.format("connected to [%6s] Port [%d] ACK", receivingPacket.getSourceMac().substring(0, 6), receivingPacket.getSourcePort()), getMac(), receivingPacket.getSourceMac(), receivingPacket.getDestinationPort());
                            registerArp(receivingPacket);
                            changePortState(receivingPacket.getSessionID(), sessionManager.getSessionInformation(receivingPacket.getSessionID()), PortState.CONNECTED);
                            handleConnectionEstablished(sessionId, port);
                        }
                        else if(portStateMap.get(receivingPacket.getDestinationPort()) == PortState.CONNECTED){
                            processData(receivingPacket.getData(), receivingPacket.getSessionID(), true);
                        }
                        else {
                            Logger.Log(Logger.LogLevel.ERROR, "connection failed ACK", getMac(), receivingPacket.getSourceMac(), receivingPacket.getDestinationPort());
                            sessionManager.deleteSession(receivingPacket.getSessionID());
                            timeoutManager.removeTimeout(receivingPacket.getSessionID());
                        }
                    }
                    case 1 -> {
                        if(portStateMap.get(receivingPacket.getDestinationPort()) == PortState.CONNECTED)
                        {
                            if(arpTable.containsKey(receivingPacket.getSourceMac()))
                            {
                                Logger.Log(Logger.LogLevel.INFO, "disconnected", getMac(), receivingPacket.getSourceMac(), receivingPacket.getDestinationPort());
                                arpTable.remove(receivingPacket.getSourceMac());
                            }else{
                                Logger.Log(Logger.LogLevel.ERROR, "connection cancelled", getMac(), receivingPacket.getSourceMac(), receivingPacket.getDestinationPort());
                            }

                            responsePacket = new Packet(
                                    receivingPacket.getSessionID(),
                                    sessionManager.getSessionInformation(receivingPacket.getSessionID()),
                                    HandshakeData.FIN_ACK,
                                    null,
                                    getMac()
                            );
                            releasePort(port);
                        }else{
                            Logger.Log(Logger.LogLevel.ERROR, "invalid packet FIN", getMac(), receivingPacket.getSourceMac(), receivingPacket.getDestinationPort());
                        }
                    }
                    case 101 -> {
                        if(arpTable.containsKey(receivingPacket.getSourceMac()) &&
                                portStateMap.get(receivingPacket.getDestinationPort()) == PortState.CONNECTED)
                        {
                            Logger.Log(Logger.LogLevel.INFO, "disconnected FIN ACK", getMac(), receivingPacket.getSourceMac(), receivingPacket.getDestinationPort());
                            arpTable.remove(receivingPacket.getSourceMac());
                        }else if (portStateMap.get(receivingPacket.getDestinationPort()) == PortState.CONNECTING){
                            Logger.Log(Logger.LogLevel.ERROR, "connection failed FIN ACK", getMac(), receivingPacket.getSourceMac(), receivingPacket.getDestinationPort());
                            
                        }
                        else{
                            Logger.Log(Logger.LogLevel.ERROR, "invalid packet FIN ACK", getMac(), receivingPacket.getSourceMac(), receivingPacket.getDestinationPort());
                            return;
                        }
                        releasePort(port);
                    }
                    case 0 -> {
                        processData(receivingPacket.getData(), receivingPacket.getSessionID(), false);
                    }
                    default -> {
                        Logger.Log(Logger.LogLevel.INFO, String.format("code [%d] received", code), getMac(), receivingPacket.getSourceMac(), receivingPacket.getDestinationPort());
                    }
                }
                if(responsePacket != null) {
                    send(responsePacket);
                }
            }
        }
    }

    protected void processData(byte[] data, String sessionId, boolean ack)
    {
        SessionInformation info = sessionManager.getSessionInformation(sessionId);
        Object object;

        try {
            object = ByteConvertor.deSerialize(data);
        } catch (ClassNotFoundException | IOException e) {
            object = null;
        }

        if(object == null)
        {
            Logger.Log(Logger.LogLevel.DEBUG, String.format("Empty Data received [%s]", ack ? "ACK" : ""), getMac(), info.destinationMac(), info.sourcePort());
            if(dataReceiverMapper.containsKey(EmptyData.class))
                triggerReceiver(sessionId, new EmptyData());
            return;
        }
        Logger.Log(Logger.LogLevel.DEBUG, String.format("Data received [%s]", object), getMac(), info.destinationMac(), info.sourcePort());
        if(dataReceiverMapper.containsKey(object.getClass()))
            triggerReceiver(sessionId, object);

    }

    @Override
    public void releasePort(int port)
    {
        Logger.Log(Logger.LogLevel.INFO, "released", getMac(), port);
        String sessionId = sessionManager.getSessionId(port);
        changePortState(sessionId, sessionManager.getSessionInformation(sessionId), PortState.OPEN);
    }

    private void registerArp(Packet packet)
    {
        arpTable.put(packet.getSourceMac(), packet.getDestinationPort());
    }

    private void changePortState(Integer port, PortState state)
    {
        if(state == null)
        {
            sessionManager.deleteSession(port);
            portStateMap.remove(port);
            timeoutManager.removeTimeout(sessionManager.getSessionId(port));
        }
        else
            // open
            changePortState(port, state, null, null, null);
    }

    private void changePortState(String sessionId, SessionInformation info, PortState state)
    {
        changePortState(info.sourcePort(), state, sessionId, info.destinationPort(), info.destinationMac());
    }

    private void changePortState(Integer port, PortState state, String sessionId, Integer destinationPort, String destinationMac)
    {
        if(state == PortState.OPEN)
        {
            if(sessionId != null)
            {
                sessionManager.deleteSession(sessionId);
                timeoutManager.removeTimeout(sessionId);
            }
            else if(!(destinationPort == null && destinationMac == null))
                throw new RuntimeException("Error !!!");
        }
        else if(state == null)
        {
            if(sessionId != null)
            {
                sessionManager.deleteSession(sessionId);
                portStateMap.remove(port);
                timeoutManager.removeTimeout(sessionId);
            }else{
                throw new RuntimeException("Error !!");
            }
        }
        else if(portStateMap.get(port) == PortState.OPEN && state == PortState.CONNECTING)
        {
            // SYN
            sessionManager.register(sessionId, port, destinationPort, destinationMac);
            timeoutManager.registerTimeout(sessionId, ConnectionHandler.class, timeout, this::handleConnectionTimeout);
        }
        else if(portStateMap.get(port) == PortState.CONNECTING && state == PortState.CONNECTED)
        {
            // Syn Ack // Ack
            sessionManager.updateSession(sessionId, port, destinationPort, destinationMac);
            timeoutManager.removeTimeout(sessionId, ConnectionHandler.class);
            portStateMap.put(port, state);
        }
        else if(portStateMap.get(port) == PortState.CONNECTED && state == PortState.OPEN)
        {
            sessionManager.deleteSession(sessionId);
            timeoutManager.removeTimeout(sessionId);
        }
        else{
            Logger.Log(Logger.LogLevel.ERROR, String.format("Invalid State Transition to [%s]", state.name()), getMac(), destinationMac, port);
            errorHandler(sessionId, NetworkErrorType.INVALID_STATE_TRANSITION);
            return;
        }
        portStateMap.put(port, state);
    }

    @Override
    public boolean isConnected(int port) {
        if(portStateMap.containsKey(port) && portStateMap.get(port) == PortState.CONNECTED)
            return true;
        return false;
    }

    @Override
    public void connect(int sourcePort) {
        connect(sourcePort, sourcePort);
    }

    @Override
    public void connect(int sourcePort, ConnectionEstablishedHandler handler) {
        connectionEstablishedHandlerMap.put(sourcePort, handler);
        connect(sourcePort, sourcePort);
    }

    @Override
    public void connect(int sourcePort, int destinationPort, ConnectionEstablishedHandler handler) {
        connectionEstablishedHandlerMap.put(sourcePort, handler);
        connect(sourcePort, destinationPort);
    }

    @Override
    public void connect(int sourcePort, int destinationPort) {
        if(!openPort(sourcePort))
            return;

        String sessionId = sessionManager.generateSession();
        changePortState(sourcePort, PortState.CONNECTING, sessionId, destinationPort, null);
        Packet packet = new Packet(
                sessionId,
                sessionManager.getSessionInformation(sessionId),
                HandshakeData.SYN,
                null,
                getMac()
        );
        Logger.Log(Logger.LogLevel.INFO, "connecting", getMac(), sourcePort);
        send(packet);
    }

    @Override
    public void disconnect(String sessionId) {
        SessionInformation sessionInformation = sessionManager.getSessionInformation(sessionId);
        Logger.Log(Logger.LogLevel.INFO, "disconnecting", getMac(), sessionInformation.sourcePort());
        send(
                new Packet(
                        sessionId,
                        sessionInformation,
                        HandshakeData.FIN,
                        null,
                        this.getMac()
                )
        );
    }

    @Override
    public void handleConnectionTimeout(String sessionId, Integer retryNum) {
        Logger.Log(Logger.LogLevel.ERROR, "timeout", getMac(), sessionManager.getPort(sessionId));
        if(retryNum < CONNECTION_TIMEOUT_RETRY)
        {
            Packet packet = new Packet(
                    sessionId,
                    sessionManager.getSessionInformation(sessionId),
                    HandshakeData.SYN,
                    null,
                    getMac()
            );
            Logger.Log(Logger.LogLevel.INFO, "connecting RST", getMac(), sessionManager.getSessionInformation(sessionId).sourcePort());
            send(packet);
            timeoutManager.updateTimeout(sessionId, ConnectionHandler.class);
            return;
        }
        if(sessionManager.getSessionInformation(sessionId).destinationMac() != null)
            disconnect(sessionId);
        errorHandler(sessionId, NetworkErrorType.TIMEOUT);
        sessionManager.deleteSession(sessionId);
        timeoutManager.removeTimeout(sessionId, ConnectionHandler.class);
    }

    @Override
    public void handleConnectionEstablished(String sessionId, Integer port) {
        Logger.Log(Logger.LogLevel.INFO, "Connection established", getMac(), port);
        if(connectionEstablishedHandlerMap.containsKey(port))
            connectionEstablishedHandlerMap.get(port).established(port);
    }

    @Override
    public void disconnect(Integer port) {
        disconnect(sessionManager.getSessionId(port));
    }

    @Override
    public void send(Packet packet) {
        sendingQueue.offer(packet);
    }

    @Override
    public void receive(Packet packet) {
        receivingQueue.offer(packet);
    }

    @Override
    public void sendData(Integer port, Serializable data) {
        if(portStateMap.getOrDefault(port, PortState.CLOSED) != PortState.CONNECTED)
        {
            Logger.Log(Logger.LogLevel.ERROR, String.format("Failed to send data [%s]", data), getMac(), port);
            errorHandler(port, NetworkErrorType.NOT_CONNECTED);
            return;
        }
        String sessionId = sessionManager.getSessionId(port);
        SessionInformation sessionInformation = sessionManager.getSessionInformation(sessionId);

        Packet packet = new Packet(
                sessionId,
                sessionInformation,
                HandshakeData.EMPTY,
                ByteConvertor.serialize(data),
                this.getMac()

        );
        Logger.Log(Logger.LogLevel.INFO, String.format("Data Sent[%s]", data), getMac(), sessionInformation.sourcePort());
        send(packet);
    }

    @Override
    public void triggerReceiver(String sessionId, Object object) {
        dataReceiverMapper.get(object.getClass()).dataReceived(object, sessionManager.getPort(sessionId));
    }

    public static void main(String[] args ) throws InterruptedException {
        Logger.Log_Filter = Logger.LogLevel.INFO;
        ArrayList<PositionCommand> positionData = new ArrayList<>()
        {
            {
                String message = "";
                for(int i = 0; i < 4000; i++)
                    message += "Keita";
                add(new PositionCommand(100, message));
            }
        };

        Network network1 = new NetworkImp(0.01F);
        NetworkComponent component1 = new SlowStartApplicationNetworkComponentImp(network1, 0.01F);
        component1.openPort(11);
        component1.enabledPortTransfer(11);
        NetworkComponent component2 = new SlowStartApplicationNetworkComponentImp(network1, 0.01F);
        component2.openPort(10);
        NetworkComponent component3 = new SlowStartApplicationNetworkComponentImp(network1, 0.01F);
        component3.openPort(10);

//        component1.connect(10);
//        component1.connect(20);
        component2.connect(10, 11, port -> {
            component2.sendData(port, positionData);
//            component2.sendData(port, positionData);
        });
//        component3.connect(10, 11, port -> {
//            component3.sendData(port, positionData);
//        });

//        component3.connect(20, port -> {
////            component3.sendData(port, positionData);
//        });

        component1.addDataReceiver(positionData.getClass(), ((data, sessionId) -> {
            ArrayList<PositionCommand> array = data;
            for(PositionCommand p: array)
                System.out.println(p);
        }));
//        component4.addDataReceiver(positionData.getClass(), ((data, sessionId) -> {
//            ArrayList<PositionCommand> array = data;
//            for(PositionCommand p: array)
//                System.out.println(p);
//        }));

        int cnt = 0;

        while(true)
        {
            Thread.sleep(30);
            network1.update(0.03F);
//            if(cnt==100)
//            {
//                component3.setDataReceiver(s ->{
//                    System.out.println(((String)s));
//                    component3.sendData(20, Integer.parseInt((String)s) + 1 + "");
//                });
//                component1.setDataReceiver(s ->{
//                    System.out.println(((String)s));
//                    component1.sendData(20, Integer.parseInt((String)s) + 1 + "");
//                });
//                component1.sendData(20, "1");
//
//            }
        }
    }
}
