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
    private static final long DEFAULT_TIMEOUT = 5000000;

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
                System.out.printf("[%6s-%6s] Open Port [%d] closed\n", getMac(), "", port);
                portStateMap.remove(port);
                sessionManager.deleteSession(port);
            }
            case CONNECTED, CONNECTING -> {
                System.out.printf("[%6s-%6s] Port [%d] disconnecting\n", getMac(), "", port);
                Map<String, SessionInformation> sessionInformationMap = sessionManager.getSessionInformationMap(port);
                List<String> sessionIds = new ArrayList<>(sessionManager.getSessionId(port));
                for(String sessionId: sessionIds)
                {
                    SessionInformation sessionInformation = sessionInformationMap.get(sessionId);
                    send(
                            new Packet<>(
                                    sessionId,
                                    new HandshakeData(false, false, false, true),
                                    sessionInformation.sourcePort(),
                                    sessionInformation.destinationPort(),
                                    this.getMac(),
                                    sessionInformation.destinationMac()
                            )
                    );
                    sessionManager.deleteSession(sessionId);
                }
            }
            default -> {
                System.out.printf("[%6s-%6s] Port [%d] not open\n", getMac().substring(0, 6), "", port);
            }
        }
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
                Object data = receivingPacket.getData();
                if(receivingPacket.getSourceMac() == null)
                {
                    System.out.printf("[%6s-%6s] Port [%d] connection rejected SYN\n", getMac().substring(0, 6), "", receivingPacket.getDestinationPort());
                    sessionManager.deleteSession(receivingPacket.getSessionID());
                    return;
                }

                if(data instanceof HandshakeData d)
                {
                    Packet<HandshakeData> responsePacket = null;
                    int code = (d.isSyn() ? 1000:0) + (d.isAck() ? 100:0) + (d.isRst() ? 10:0) + (d.isFin() ? 1:0);
                    if(!portStateMap.containsKey(receivingPacket.getDestinationPort()))
                    {
                        System.out.printf("[%6s-%6s] Port [%d] is closed \n", getMac().substring(0, 6), receivingPacket.getSourceMac().substring(0, 6), receivingPacket.getDestinationPort());
                        responsePacket = new Packet<>(
                                receivingPacket,
                                new HandshakeData(false, true, true, false),
                                receivingPacket.getDestinationPort(),
                                receivingPacket.getSourcePort(),
                                null,
                                receivingPacket.getSourceMac()
                        );
                        send(responsePacket);
                        sessionManager.deleteSession(receivingPacket.getSessionID());
                        return;
                    }
                    switch (code)
                    {
                        // SYN
                        case 1000 -> {
                            if(portStateMap.get(receivingPacket.getDestinationPort()) == PortState.OPEN)
                            {
                                System.out.printf("[%6s-%6s] Port [%d] connecting SYN\n", getMac().substring(0, 6), receivingPacket.getSourceMac().substring(0, 6), receivingPacket.getDestinationPort());
                                changePortState(receivingPacket.getDestinationPort(), PortState.CONNECTING);
                                responsePacket = new Packet<>(
                                        receivingPacket,
                                        new HandshakeData(true, true, false, false),
                                        getMac()
                                );
                            }else{
                                System.out.printf("[%6s-%6s] Port [%d] in use\n", getMac().substring(0, 6), receivingPacket.getSourceMac().substring(0, 6), receivingPacket.getDestinationPort());
                                sessionManager.deleteSession(receivingPacket.getSessionID());
                            }
                        }
                        case 1100 -> {
                            if(portStateMap.get(receivingPacket.getDestinationPort()) == PortState.CONNECTING)
                            {
                                System.out.printf("[%6s-%6s] Port [%d] connected to [%s] Port [%d]\n", getMac().substring(0, 6), receivingPacket.getSourceMac().substring(0, 6), receivingPacket.getDestinationPort(), receivingPacket.getSourceMac(), receivingPacket.getSourcePort());
                                changePortState(receivingPacket.getDestinationPort(), PortState.CONNECTED);
                                arpTable.put(receivingPacket.getSourceMac(), receivingPacket.getDestinationPort());
                                responsePacket = new Packet<>(
                                        receivingPacket,
                                        new HandshakeData(false, true, false, false),
                                        getMac()
                                );
                            }else{
                                System.out.printf("[%6s-%6s] Port [%d] invalid packet SYN ACK\n", getMac().substring(0, 6), receivingPacket.getSourceMac().substring(0, 6), receivingPacket.getDestinationPort());
                                responsePacket = new Packet<>(
                                        receivingPacket,
                                        new HandshakeData(false, true, true, true),
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
                                changePortState(receivingPacket.getDestinationPort(), PortState.CONNECTED);
                            }else{
                                System.out.printf("[%6s-%6s] Port [%d] connection failed ACK\n", getMac().substring(0, 6), receivingPacket.getSourceMac().substring(0, 6), receivingPacket.getDestinationPort());
                                sessionManager.deleteSession(receivingPacket.getSessionID());
                            }
                        }
                        case 1 -> {
                            if(portStateMap.get(receivingPacket.getDestinationPort()) == PortState.CONNECTED
                            )
                            {
                                if(arpTable.containsKey(receivingPacket.getSourceMac()))
                                {
                                    System.out.printf("[%6s-%6s] Port [%d] disconnected \n", getMac().substring(0, 6), receivingPacket.getSourceMac().substring(0, 6), receivingPacket.getDestinationPort());
                                    arpTable.remove(receivingPacket.getSourceMac());
                                }else{
                                    System.out.printf("[%6s-%6s] Port [%d] connection cancelled \n", getMac().substring(0, 6), receivingPacket.getSourceMac().substring(0, 6), receivingPacket.getDestinationPort());
                                }

                                changePortState(receivingPacket.getDestinationPort(), PortState.OPEN);
                                responsePacket = new Packet<>(
                                        receivingPacket,
                                        new HandshakeData(false, true, false, true),
                                        getMac()
                                );
                            }else{
                                System.out.printf("[%6s-%6s] Port [%d] invalid packet FIN \n", getMac().substring(0, 6), receivingPacket.getSourceMac().substring(0, 6), receivingPacket.getDestinationPort());
                            }

                            sessionManager.deleteSession(receivingPacket.getSessionID());
                        }
                        case 101 -> {
                            if(arpTable.containsKey(receivingPacket.getSourceMac()) &&
                                    portStateMap.get(receivingPacket.getDestinationPort()) == PortState.CONNECTED
                            )
                            {
                                System.out.printf("[%6s-%6s] Port [%d] disconnected FIN ACK\n", getMac().substring(0, 6), receivingPacket.getSourceMac().substring(0, 6), receivingPacket.getDestinationPort());
                                arpTable.remove(receivingPacket.getSourceMac());
                                changePortState(receivingPacket.getDestinationPort(), PortState.OPEN);
                            }else if (portStateMap.get(receivingPacket.getDestinationPort()) == PortState.CONNECTING
                            ){
                                System.out.printf("[%6s-%6s] Port [%d] connection failed FIN ACK\n", getMac().substring(0, 6), receivingPacket.getSourceMac().substring(0, 6), receivingPacket.getDestinationPort());
                                changePortState(receivingPacket.getDestinationPort(), PortState.OPEN);
                            }
                            else{
                                System.out.printf("[%6s-%6s] Port [%d] invalid packet FIN ACK\n", getMac().substring(0, 6), receivingPacket.getSourceMac().substring(0, 6), receivingPacket.getDestinationPort());
                            }
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
    }

    private void registerArp(Packet<?> packet)
    {
        arpTable.put(packet.getSourceMac(), packet.getDestinationPort());
    }

    // TODO -> Port to session is not one to one
    private void changePortState(Integer port, PortState state)
    {
        portStateMap.put(port, state);
        if(state == PortState.OPEN)
            sessionManager.deleteSession(port);

    }

    @Override
    public boolean isConnected(int port) {
        return false;
    }

    @Override
    public void connect(int sourcePort) {
        if(!openPort(sourcePort))
            return;

        changePortState(sourcePort, PortState.CONNECTING);
        Packet<HandshakeData> packet = new Packet<>(
                new HandshakeData(true, false, false, false),
                sourcePort,
                sourcePort,
                getMac(),
                null);
        System.out.printf("[%6s-%6s] Port [%d] connecting\n", getMac().substring(0, 6), "", sourcePort);
        send(packet);
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
    public void triggerTimeout(Integer port, SessionInformation sessionInformation) {
        changePortState(port, PortState.OPEN);
        System.out.printf("[%6s-] Port [%d] timeout\n", getMac().substring(0, 6), port);
    }

    public static void main(String[] args )
    {
        Network network = new NetworkImp();
        NetworkComponent component1 = new NetworkComponentImp(network, 0.08F);
        NetworkComponent component2 = new NetworkComponentImp(network, 0.08F);
        component2.openPort(10);
        NetworkComponent component3 = new NetworkComponentImp(network, 0.08F);
        component3.openPort(20);

        network.addToNetwork(component1);
        network.addToNetwork(component2);
        network.addToNetwork(component3);

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
//            if(cnt == 200)
//                component1.closePort(10);

        }
    }
}
