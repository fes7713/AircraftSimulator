package aircraftsimulator.GameObject.Aircraft.Communication;

import java.io.IOException;
import java.util.*;

// TODO -> Unreleased session in code.

public class NetworkComponentImp implements NetworkComponent, TimeoutHandler{
    private final Network network;
    private final String mac;

    private final Map<Integer, PortState> portStateMap;
    private final Map<String, Integer> arpTable;
    private final SessionManager sessionManager;

    private final Queue<Packet<?>> sendingQueue;
    private final Queue<Packet<?>> receivingQueue;
    private NetworkMode networkMode;

    private final float updateInterval;
    private float timeClock;

    private long timeout;
    private static final long DEFAULT_TIMEOUT = 5000;

    public NetworkComponentImp(Network network, float updateInterval)
    {
        this.network = network;
        mac = UUID.randomUUID().toString();
        portStateMap = new HashMap<>();

        arpTable = new HashMap<>();
        sessionManager = new SessionManager(this);

        sendingQueue = new ArrayDeque<>();
        receivingQueue = new ArrayDeque<>();
        networkMode = NetworkMode.IDLE;

        this.updateInterval = updateInterval;
        timeClock = updateInterval;

        timeout = DEFAULT_TIMEOUT;
    }

    @Override
    public String getMac() {
        return mac;
    }

    @Override
    public void update(float delta) {
        if(timeClock < 0)
        {
            sessionManager.checkTimeout(timeout);
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
                changePortState(port, PortState.OPEN, null);
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
                disconnect(port);
            }
            default -> {
                System.out.printf("[%6s-%6s] Port [%d] not open\n", getMac().substring(0, 6), "", port);
            }
        }
        changePortState(port, null, null);
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
                Packet sendingPacket = sendingQueue.poll();
                Integer sendingPort = sendingPacket.getSourcePort();
                String destinationMac = sendingPacket.getDestinationMac();
//                if(destinationMac != null && newSessionFlag)
//                    throw new RuntimeException("Invalid state of program");

                if(destinationMac != null)
                    network.sendTo(destinationMac, sendingPacket);
                else
                    network.broadcast(sendingPacket, getMac(), sessionManager);

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
//                    sessionManager.deleteSession(receivingPacket.getSessionID());
                    return;
                }

                Packet<HandshakeData> responsePacket = null;
                int code = (handshakeData.isSyn() ? 1000:0) + (handshakeData.isAck() ? 100:0) + (handshakeData.isRst() ? 10:0) + (handshakeData.isFin() ? 1:0);
                if(!portStateMap.containsKey(receivingPacket.getDestinationPort()))
                {
                    System.out.printf("[%6s-%6s] Port [%d] is closed \n", getMac().substring(0, 6), receivingPacket.getSourceMac().substring(0, 6), receivingPacket.getDestinationPort());
                    responsePacket = new Packet<>(
                            receivingPacket,
                            new HandshakeData(false, true, true, false),
                            null,
                            receivingPacket.getDestinationPort(),
                            receivingPacket.getSourcePort(),
                            null,
                            receivingPacket.getSourceMac()
                    );
                    send(responsePacket);
                    sessionManager.deleteSession(receivingPacket.getSessionID());
                    return;
                }

                Integer port = receivingPacket.getDestinationPort();
                if(portStateMap.get(port) != PortState.OPEN && !sessionManager.isRegistered(receivingPacket.getSessionID(), receivingPacket.getSourceMac()))
                {
                    System.out.printf("[%6s-%6s] Port [%d] Session id [%6s] is not registered\n", getMac().substring(0, 6), receivingPacket.getSourceMac().substring(0, 6), receivingPacket.getDestinationPort(), receivingPacket.getSessionID());
                    changePortState(port, PortState.OPEN, null);
                    return;
                }

                switch (code)
                {
                    // SYN
                    case 1000 -> {
                        if(portStateMap.get(port) == PortState.OPEN)
                        {
                            System.out.printf("[%6s-%6s] Port [%d] connecting SYN\n", getMac().substring(0, 6), receivingPacket.getSourceMac().substring(0, 6), receivingPacket.getDestinationPort());
                            responsePacket = new Packet<>(
                                    receivingPacket,
                                    new HandshakeData(true, true, false, false),
                                    null,
                                    getMac()
                            );
                            changePortState(responsePacket, PortState.CONNECTING);

                        }else{
                            System.out.printf("[%6s-%6s] Port [%d] in use\n", getMac().substring(0, 6), receivingPacket.getSourceMac().substring(0, 6), receivingPacket.getDestinationPort());
                            sessionManager.deleteSession(receivingPacket.getSessionID());
                        }
                    }
                    // SYN ACK
                    case 1100 -> {
                        if(portStateMap.get(port) == PortState.CONNECTING)
                        {
                            System.out.printf("[%6s-%6s] Port [%d] connected to [%s] Port [%d]\n", getMac().substring(0, 6), receivingPacket.getSourceMac().substring(0, 6), receivingPacket.getDestinationPort(), receivingPacket.getSourceMac(), receivingPacket.getSourcePort());
                            arpTable.put(receivingPacket.getSourceMac(), receivingPacket.getDestinationPort());
                            responsePacket = new Packet<>(
                                    receivingPacket,
                                    new HandshakeData(false, true, false, false),
                                    null,
                                    getMac()
                            );
                            changePortState(responsePacket, PortState.CONNECTED);
                        }else{
                            System.out.printf("[%6s-%6s] Port [%d] invalid packet SYN ACK\n", getMac().substring(0, 6), receivingPacket.getSourceMac().substring(0, 6), receivingPacket.getDestinationPort());
                            responsePacket = new Packet<>(
                                    receivingPacket,
                                    new HandshakeData(false, true, true, true),
                                    null,
                                    receivingPacket.getDestinationPort(),
                                    receivingPacket.getSourcePort(),
                                    null,
                                    receivingPacket.getSourceMac()
                            );
                            sessionManager.deleteSession(receivingPacket.getSessionID());
                        }
                    }
                    case 100 -> {
                        if(portStateMap.get(receivingPacket.getDestinationPort()) == PortState.CONNECTING){
                            System.out.printf("[%6s-%6s] Port [%d] connected to [%s] Port [%d] ACK\n", getMac().substring(0, 6), receivingPacket.getSourceMac().substring(0, 6), receivingPacket.getDestinationPort(), receivingPacket.getSourceMac(), receivingPacket.getSourcePort());
                            registerArp(receivingPacket);
                            changePortState(receivingPacket.getDestinationPort(), PortState.CONNECTED,
                                    receivingPacket.getSessionID(), receivingPacket.getSourcePort(), receivingPacket.getSourceMac());
                        }
                        else if(portStateMap.get(receivingPacket.getDestinationPort()) == PortState.CONNECTED){
                            System.out.printf("[%6s-%6s] Port Data Received ACK [%s]\n", getMac().substring(0, 6), receivingPacket.getSourceMac().substring(0, 6), receivingPacket.getDestinationPort(), receivingPacket.getSourceMac(), receivingPacket.getSourcePort(), receivingPacket.getData().toString());
                        }
                        else {
                            System.out.printf("[%6s-%6s] Port [%d] connection failed ACK\n", getMac().substring(0, 6), receivingPacket.getSourceMac().substring(0, 6), receivingPacket.getDestinationPort());
                            sessionManager.deleteSession(receivingPacket.getSessionID());
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

                            releasePort(receivingPacket);
                            responsePacket = new Packet<>(
                                    receivingPacket,
                                    new HandshakeData(false, true, false, true),
                                    null,
                                    getMac()
                            );
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
                        releasePort(receivingPacket);
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

    private void registerArp(Packet<?> packet)
    {
        arpTable.put(packet.getSourceMac(), packet.getDestinationPort());
    }

    private void changePortState(Packet<?> responsePacket, PortState state)
    {
        changePortState(responsePacket.getSourcePort(), state, responsePacket);
    }

    private void changePortState(Integer port, PortState state, Packet<?> packet)
    {
        if(state == null)
        {
            sessionManager.deleteSession(port);
            portStateMap.remove(port);
        }
        else if(packet != null)
            changePortState(port, state, packet.getSessionID(), packet.getDestinationPort(), packet.getDestinationMac());
        else
            // open
            changePortState(port, state, null, null, null);
    }

    private void releasePort(Packet<?> receivingPacket)
    {
        changePortState(receivingPacket.getDestinationPort(), PortState.OPEN,
                receivingPacket.getSessionID(), receivingPacket.getSourcePort(), receivingPacket.getSourceMac());
    }

    // TODO -> Port to session is not one to one
    private void changePortState(Integer port, PortState state, String sessionId, Integer destinationPort, String destinationMac)
    {
        if(state == PortState.OPEN)
        {
            if(sessionId != null)
                sessionManager.deleteSession(sessionId);
            else if(!(destinationPort == null && destinationMac == null))
                throw new RuntimeException("Error !!!");
        }
        else if(state == null)
        {
            if(sessionId != null)
            {
                sessionManager.deleteSession(sessionId);
                portStateMap.remove(port);
            }else{
                throw new RuntimeException("Error !!");
            }
        }
        else if(portStateMap.get(port) == PortState.OPEN && state == PortState.CONNECTING)
        {
            // Syn
            if(sessionId != null)
                sessionManager.register(sessionId, port, destinationPort, destinationMac);
        }
        else if(portStateMap.get(port) == PortState.CONNECTING && state == PortState.CONNECTED)
        {
            // Syn Ack
            sessionManager.updateSession(sessionId, port, destinationPort, destinationMac);
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
        if(!openPort(sourcePort))
            return;

        changePortState(sourcePort, PortState.CONNECTING, null);
        Packet<HandshakeData> packet = new Packet<>(
                new HandshakeData(true, false, false, false),
                null,
                sourcePort,
                sourcePort,
                getMac(),
                null);
        System.out.printf("[%6s-%6s] Port [%d] connecting\n", getMac().substring(0, 6), "", sourcePort);
        send(packet);
    }

    @Override
    public void disconnect(Integer port) {
        System.out.printf("[%6s-%6s] Port [%d] disconnecting\n", getMac().substring(0, 6), "", port);
        String sessionId = sessionManager.getSessionId(port);
        SessionInformation sessionInformation = sessionManager.getSessionInformation(sessionId);
        send(
                new Packet<>(
                        sessionId,
                        new HandshakeData(false, false, false, true),
                        null,
                        sessionInformation.sourcePort(),
                        sessionInformation.destinationPort(),
                        this.getMac(),
                        sessionInformation.destinationMac()
                )
        );
    }

    @Override
    public void send(Packet<?> packet) {
        sendingQueue.offer(packet);
    }

    @Override
    public void receive(Packet<?> packet) {
        receivingQueue.offer(packet);
    }

    @Override
    public void sendData(String sessionId, Object data) {
        if(!sessionManager.validateSessionId(sessionId))
            return;
        SessionInformation sessionInformation = sessionManager.getSessionInformation(sessionId);

        Packet<Object> packet = new Packet<>(
                sessionId,
                new HandshakeData(false, false, false, false),
                data,
                sessionInformation.sourcePort(),
                sessionInformation.destinationPort(),
                this.getMac(),
                sessionInformation.destinationMac()
        );
        System.out.printf("[%6s-%6s] Port [%d] Data [%s]\n", getMac().substring(0, 6), "", sessionInformation.sourcePort(), data.toString());
        send(packet);
    }

    @Override
    public void triggerTimeout(Integer port, SessionInformation sessionInformation) {
        if(portStateMap.get(port) == PortState.CONNECTING) {
            changePortState(port, PortState.OPEN, sessionManager.getSessionId(port), sessionInformation.destinationPort(), sessionInformation.destinationMac());
            System.out.printf("[%6s-] Port [%d] timeout\n", getMac().substring(0, 6), port);
        }
    }

    public static void main(String[] args )
    {
        Network network = new NetworkImp();
        NetworkComponent component1 = new NetworkComponentImp(network, 0.08F);
        NetworkComponent component2 = new NetworkComponentImp(network, 0.08F);
        component2.openPort(10);
        NetworkComponent component3 = new NetworkComponentImp(network, 0.08F);
        component3.openPort(20);
        NetworkComponent component4 = new NetworkComponentImp(network, 0.08F);

        network.addToNetwork(component1);
        network.addToNetwork(component2);
        network.addToNetwork(component3);
        network.addToNetwork(component4);

        component1.connect(10);
        component1.connect(20);

        int cnt = 0;

        PositionCommand command = new PositionCommand(1, "");
        try {
            byte[] array = ByteConvertor.serialize(command);
            String string = ByteConvertor.convert(array);
            System.out.println(string);

            byte[][] arrays = ByteConvertor.serialize(command, 32);
            System.out.println(ByteConvertor.convert(arrays));

            PositionCommand convComand = ByteConvertor.deSerialize(array);
            System.out.println(convComand.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

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
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            component4.update(0.03F);
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if(cnt == 100)
            {
                System.out.println("Disconnect");
                component1.disconnect(10);
            }
            if(cnt == 160)
            {
                System.out.println("Connect 10");
                component1.connect(10);
            }
            if(cnt == 200)
            {
                System.out.println("Connect 30");
                component1.connect(30);
            }
            if(cnt == 300)
            {
                System.out.println("Open port 30");
                component4.openPort(30);
            }
            if(cnt == 320)
            {
                System.out.println("Connect 30");
                component1.connect(30);
            }

        }
    }
}
