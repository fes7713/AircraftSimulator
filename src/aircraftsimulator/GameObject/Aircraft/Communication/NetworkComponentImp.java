package aircraftsimulator.GameObject.Aircraft.Communication;

import aircraftsimulator.GameObject.Aircraft.Communication.Data.EmptyData;
import aircraftsimulator.GameObject.Aircraft.Communication.Handler.ConnectionTimeoutHandler;
import aircraftsimulator.GameObject.Aircraft.Communication.Timeout.TimeoutManager;
import aircraftsimulator.GameObject.Aircraft.Communication.Timeout.TimeoutManagerImp;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

// TODO -> Unreleased session in code.

public class NetworkComponentImp implements NetworkComponent, ConnectionTimeoutHandler {
    protected final Network network;
    private final String mac;

    protected final Map<Integer, PortState> portStateMap;
    private final Map<String, Integer> arpTable;
    protected final SessionManager sessionManager;
    protected final TimeoutManager timeoutManager;

    private final Queue<Packet> sendingQueue;
    private final Queue<Packet> receivingQueue;
    private NetworkMode networkMode;

    private final float updateInterval;
    private float timeClock;
    private final int networkSpeed;

    private final Map<Class<? extends Serializable>, DataReceiver> dataReceiverMapper;

    private final long timeout;
    private static final long DEFAULT_TIMEOUT = 5000;
    private final static int DEFAULT_NETWORK_SPEED = 5;

    public NetworkComponentImp(Network network, float updateInterval)
    {
        this.network = network;
        mac = UUID.randomUUID().toString();
        portStateMap = new HashMap<>();
        arpTable = new HashMap<>();
        sessionManager = new SessionManagerImp();
        timeoutManager = new TimeoutManagerImp();

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
                System.out.printf("[%6s-%6s] Port [%d] already open\n", getMac().substring(0, 6), "", port);
                return true;
            }
            case CONNECTED, CONNECTING -> {
                System.out.printf("[%6s-%6s] Port [%d] in use\n", getMac().substring(0, 6), "", port);
                return false;
            }
            case CLOSED ->{
                System.out.printf("[%6s-%6s] Port [%d] opened\n", getMac().substring(0, 6), "", port);
                changePortState(port, PortState.OPEN);
                return true;
            }
        }
        return false;
    }

    public void closePort(int port)
    {
        PortState portState = portStateMap.getOrDefault(port, null);

        switch (portState)
        {
            case OPEN -> {
                System.out.printf("[%6s-%6s] Open Port [%d] closed\n", getMac().substring(0, 6), "", port);
            }
            case CONNECTED, CONNECTING -> {
                System.out.printf("[%6s-%6s] Port [%d] closing\n", getMac().substring(0, 6), "", port);
//                disconnect(port);
            }
            default -> {
                System.out.printf("[%6s-%6s] Port [%d] not open\n", getMac().substring(0, 6), "", port);
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
                    System.out.printf("[%6s-%6s] Port [%d] connection rejected SYN\n", getMac().substring(0, 6), "", receivingPacket.getDestinationPort());
                    return;
                }

                Packet responsePacket = null;
                int code = (handshakeData.isSyn() ? 1000:0) + (handshakeData.isAck() ? 100:0) + (handshakeData.isRst() ? 10:0) + (handshakeData.isFin() ? 1:0);
                if(!portStateMap.containsKey(receivingPacket.getDestinationPort()))
                {
                    System.out.printf("[%6s-%6s] Port [%d] is closed \n", getMac().substring(0, 6), receivingPacket.getSourceMac().substring(0, 6), receivingPacket.getDestinationPort());
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
                    System.out.printf("[%6s-%6s] Port [%d] Session id [%6s] is not registered\n", getMac().substring(0, 6), receivingPacket.getSourceMac().substring(0, 6), receivingPacket.getDestinationPort(), receivingPacket.getSessionID());
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
                            System.out.printf("[%6s-%6s] Port [%d] connecting SYN\n", getMac().substring(0, 6), receivingPacket.getSourceMac().substring(0, 6), receivingPacket.getDestinationPort());
                            changePortState(receivingPacket.getSessionID(), receivingPacket.getSessionInformation().reverse(receivingPacket.getSourceMac()), PortState.CONNECTING);
                            responsePacket = new Packet(
                                    receivingPacket.getSessionID(),
                                    sessionManager.getSessionInformation(receivingPacket.getSessionID()),
                                    HandshakeData.SYN_ACK,
                                    null,
                                    getMac()
                            );
                        }else{
                            System.out.printf("[%6s-%6s] Port [%d] in use\n", getMac().substring(0, 6), receivingPacket.getSourceMac().substring(0, 6), receivingPacket.getDestinationPort());
                            sessionManager.deleteSession(receivingPacket.getSessionID());
                            timeoutManager.removeTimeout(receivingPacket.getSessionID());
                        }
                    }
                    // SYN ACK
                    case 1100 -> {
                        if(portStateMap.get(port) == PortState.CONNECTING)
                        {
                            System.out.printf("[%6s-%6s] Port [%d] connected to [%6s] Port [%d]\n", getMac().substring(0, 6), receivingPacket.getSourceMac().substring(0, 6), receivingPacket.getDestinationPort(), receivingPacket.getSourceMac().substring(0, 6), receivingPacket.getSourcePort());
                            arpTable.put(receivingPacket.getSourceMac(), receivingPacket.getDestinationPort());
                            changePortState(receivingPacket.getSessionID(), receivingPacket.getSessionInformation().reverse(receivingPacket.getSourceMac()), PortState.CONNECTED);
                            responsePacket = new Packet(
                                    receivingPacket.getSessionID(),
                                    sessionManager.getSessionInformation(receivingPacket.getSessionID()),
                                    HandshakeData.ACK,
                                    null,
                                    getMac()
                            );
                        }else{
                            System.out.printf("[%6s-%6s] Port [%d] invalid packet SYN ACK\n", getMac().substring(0, 6), receivingPacket.getSourceMac().substring(0, 6), receivingPacket.getDestinationPort());
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
                            System.out.printf("[%6s-%6s] Port [%d] connected to [%s] Port [%d] ACK\n", getMac().substring(0, 6), receivingPacket.getSourceMac().substring(0, 6), receivingPacket.getDestinationPort(), receivingPacket.getSourceMac(), receivingPacket.getSourcePort());
                            registerArp(receivingPacket);
                            changePortState(receivingPacket.getSessionID(), sessionManager.getSessionInformation(receivingPacket.getSessionID()), PortState.CONNECTED);
                        }
                        else if(portStateMap.get(receivingPacket.getDestinationPort()) == PortState.CONNECTED){
                            processData(receivingPacket.getData(), receivingPacket.getSessionID(), true);
                        }
                        else {
                            System.out.printf("[%6s-%6s] Port [%d] connection failed ACK\n", getMac().substring(0, 6), receivingPacket.getSourceMac().substring(0, 6), receivingPacket.getDestinationPort());
                            sessionManager.deleteSession(receivingPacket.getSessionID());
                            timeoutManager.removeTimeout(receivingPacket.getSessionID());
                        }
                    }
                    case 1 -> {
                        if(portStateMap.get(receivingPacket.getDestinationPort()) == PortState.CONNECTED)
                        {
                            if(arpTable.containsKey(receivingPacket.getSourceMac()))
                            {
                                System.out.printf("[%6s-%6s] Port [%d] disconnected \n", getMac().substring(0, 6), receivingPacket.getSourceMac().substring(0, 6), receivingPacket.getDestinationPort());
                                arpTable.remove(receivingPacket.getSourceMac());
                            }else{
                                System.out.printf("[%6s-%6s] Port [%d] connection cancelled \n", getMac().substring(0, 6), receivingPacket.getSourceMac().substring(0, 6), receivingPacket.getDestinationPort());
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
                            System.out.printf("[%6s-%6s] Port [%d] invalid packet FIN \n", getMac().substring(0, 6), receivingPacket.getSourceMac().substring(0, 6), receivingPacket.getDestinationPort());
                        }
                    }
                    case 101 -> {
                        if(arpTable.containsKey(receivingPacket.getSourceMac()) &&
                                portStateMap.get(receivingPacket.getDestinationPort()) == PortState.CONNECTED)
                        {
                            System.out.printf("[%6s-%6s] Port [%d] disconnected FIN ACK\n", getMac().substring(0, 6), receivingPacket.getSourceMac().substring(0, 6), receivingPacket.getDestinationPort());
                            arpTable.remove(receivingPacket.getSourceMac());
                        }else if (portStateMap.get(receivingPacket.getDestinationPort()) == PortState.CONNECTING){
                            System.out.printf("[%6s-%6s] Port [%d] connection failed FIN ACK\n", getMac().substring(0, 6), receivingPacket.getSourceMac().substring(0, 6), receivingPacket.getDestinationPort());
                        }
                        else{
                            System.out.printf("[%6s-%6s] Port [%d] invalid packet FIN ACK\n", getMac().substring(0, 6), receivingPacket.getSourceMac().substring(0, 6), receivingPacket.getDestinationPort());
                            return;
                        }
                        releasePort(port);
                    }
                    case 0 -> {
                        processData(receivingPacket.getData(), receivingPacket.getSessionID(), false);
                    }
                    default -> {
                        System.out.printf("[%6s-%6s] Port [%d] code [%d] received\n", getMac().substring(0, 6), receivingPacket.getSourceMac().substring(0, 6), receivingPacket.getDestinationPort(), code);
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
            System.out.printf("[%6s-%6s] Port [%d] Empty Data received [%s]\n", getMac().substring(0, 6), info.destinationMac().substring(0, 6), info.sourcePort(), ack ? "ACK" : "");
            if(dataReceiverMapper.containsKey(EmptyData.class))
                triggerReceiver(sessionId, new EmptyData());
            return;
        }

        System.out.printf("[%6s-%6s] Port [%d] Date Received [%s] received [%s]\n", getMac().substring(0, 6), info.destinationMac().substring(0, 6), info.sourcePort(), object, ack ? "ACK" : "");
        if(dataReceiverMapper.containsKey(object.getClass()))
            triggerReceiver(sessionId, object);

    }

    @Override
    public void releasePort(int port)
    {
        System.out.printf("[%6s-%6s] Port [%d] released\n", getMac().substring(0, 6), "", port);
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
            timeoutManager.registerTimeout(sessionId, ConnectionTimeoutHandler.class, timeout, this::handleConnectionTimeout);
        }
        else if(portStateMap.get(port) == PortState.CONNECTING && state == PortState.CONNECTED)
        {
            // Syn Ack // Ack
            sessionManager.updateSession(sessionId, port, destinationPort, destinationMac);
            timeoutManager.removeTimeout(sessionId, ConnectionTimeoutHandler.class);
            handleConnectionEstablished(sessionId, port);
        }
        else if(portStateMap.get(port) == PortState.CONNECTED && state == PortState.OPEN)
        {
            sessionManager.deleteSession(sessionId);
            timeoutManager.removeTimeout(sessionId);
        }
        else{
            System.out.println("Invalid State Transition");
            return;
        }
        portStateMap.put(port, state);
    }

    @Override
    public boolean isConnected(int port) {
        return false;
    }

    @Override
    public void connect(int sourcePort) {
        connect(sourcePort, sourcePort);
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
        System.out.printf("[%6s-%6s] Port [%d] connecting\n", getMac().substring(0, 6), "", sourcePort);
        send(packet);
    }

    @Override
    public void disconnect(String sessionId) {
        SessionInformation sessionInformation = sessionManager.getSessionInformation(sessionId);
        System.out.printf("[%6s-%6s] Port [%d] disconnecting\n", getMac().substring(0, 6), "", sessionInformation.sourcePort());
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
        System.out.printf("[%6s-%6s] Port [%d] timeout\n", getMac().substring(0, 6), "", sessionManager.getSessionInformation(sessionId).sourcePort());

        if(sessionManager.getSessionInformation(sessionId).destinationMac() != null)
            disconnect(sessionId);
        sessionManager.deleteSession(sessionId);
        timeoutManager.removeTimeout(sessionId, ConnectionTimeoutHandler.class);
    }

    @Override
    public void handleConnectionEstablished(String sessionId, Integer port) {
        System.out.printf("[%6s-%6s] Port [%d] Connection established\n", getMac().substring(0, 6), "", port);
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
            System.out.printf("[%6s-%6s] Port [%d] Failed to send data [%s]\n", getMac().substring(0, 6), "", port, data.toString());
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
        System.out.printf("[%6s-%6s] Port [%d] Data Sent[%s]\n", getMac().substring(0, 6), "", sessionInformation.sourcePort(), data.toString());
        send(packet);
    }

    @Override
    public void triggerReceiver(String sessionId, Object object) {
        dataReceiverMapper.get(object.getClass()).dataReceived(object, sessionId);
    }

    public static void main(String[] args )
    {

        ArrayList<PositionCommand> positionData = new ArrayList<>()
        {
            {
                String message = "";
                for(int i = 0; i < 1000; i++)
                    message += "AAAAAAAAAAAAAAA";
                add(new PositionCommand(100, message));
            }
        };

        Network network = new NetworkImp();
        NetworkComponent component1 = new ApplicationNetworkComponentImp(network, 0.01F);
        component1.openPort(10);
        component1.openPort(20);
        NetworkComponent component2 = new ApplicationNetworkComponentImp(network, 0.01F);
//        component2.openPort(10);
        NetworkComponent component3 = new ApplicationNetworkComponentImp(network, 0.01F);
//        component3.openPort(20);
        NetworkComponent component4 = new ApplicationNetworkComponentImp(network, 0.01F);
        component4.openPort(20);

        network.addToNetwork(component1);
        network.addToNetwork(component2);
        network.addToNetwork(component3);
        network.addToNetwork(component4);

//        component1.connect(10);
//        component1.connect(20);
        component2.connect(10);
        component3.connect(20);

        component1.addDataReceiver(positionData.getClass(), ((data, sessionId) -> {
            ArrayList<PositionCommand> array = data;
            for(PositionCommand p: array)
                System.out.println(p);
        }));

        int cnt = 0;

        while(true)
        {
            cnt++;

            component1.update(0.03F);
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            component2.update(0.03F);
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            component3.update(0.03F);
//            component4.update(0.03F);
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            network.update(0.03F);

            if(cnt == 150)
            {
                component3.sendData(20, positionData);
            }

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
