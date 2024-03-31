package aircraftsimulator.GameObject.Aircraft.Communication;

import java.util.*;

public class NetworkComponentImp implements NetworkComponent{
    private final Network network;
    private final String mac;

    private final Map<Integer, PortState> portStateMap;
    private final Map<String, Integer> arpTable;
    private final Map<Integer, Packet> portLatestPacketMap;

    private final Queue<Packet> sendingQueue;
    private final Queue<Packet> receivingQueue;
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
        portLatestPacketMap = new HashMap<>();

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
            checkSessions();
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
                System.out.printf("[%s] Port [%d] already open\n", getMac(), port);
                return true;
            }
            case CONNECTED, CONNECTING -> {
                System.out.printf("[%s] Port [%d] in use\n", getMac(), port);
                return false;
            }
            case CLOSED ->{
                System.out.printf("[%s] Port [%d] opened\n", getMac(), port);
                portStateMap.put(port, PortState.OPEN);
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
                System.out.printf("[%s] Open Port [%d] closed\n", getMac(), port);
                portStateMap.remove(port);
                portLatestPacketMap.remove(port);
            }
            case CONNECTED, CONNECTING -> {
                System.out.printf("[%s] Port [%d] disconnecting\n", getMac(), port);
                send(
                        new Packet<>(
                                portLatestPacketMap.get(port),
                                new HandshakeData(false, false, true, null, getMac()),
                                portLatestPacketMap.get(port).getSourcePort(),
                                portLatestPacketMap.get(port).getDestinationPort()
                        )
                );
            }
            default -> {
                System.out.printf("[%s] Port [%d] not open\n", getMac(), port);
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
                boolean newSessionFlag = false;

                if(!portLatestPacketMap.containsKey(sendingPacket.getSourcePort()))
                {
                    if(!(sendingPacket.getData() instanceof HandshakeData hd && hd.isAck()))
                    {
                        portLatestPacketMap.put(sendingPacket.getSourcePort(), sendingPacket);
                        newSessionFlag = true;
                    }
                }

                String destinationMac = null;
                for(Map.Entry<String, Integer> arpPortPair: arpTable.entrySet())
                {
                    if(arpPortPair.getValue() == sendingPacket.getSourcePort())
                        destinationMac = arpPortPair.getKey();
                }
                if(destinationMac != null && newSessionFlag)
                    throw new RuntimeException("Invalid state of program");

                if(destinationMac != null)
                    network.sendTo(destinationMac, sendingPacket);
                else
                    network.broadcast(sendingPacket, getMac());

                if(sendingQueue.isEmpty())
                    networkMode = NetworkMode.IDLE;
            }
            case RECEIVING -> {
                Packet receivingPacket = receivingQueue.poll();
                Object data = receivingPacket.getData();

                if(data instanceof HandshakeData d)
                {
                    Packet<HandshakeData> responsePacket = null;
                    int code = (d.isSyn() ? 100:0) + (d.isAck() ? 10:0) + (d.isFin() ? 1:0);
                    switch (code)
                    {
                        // SYN
                        case 100 -> {
                            if(d.getRequestingPort() != null &&
                                    portStateMap.containsKey(d.getRequestingPort()))
                            {
                                if(portStateMap.get(d.getRequestingPort()) == PortState.OPEN)
                                {
                                    System.out.printf("[%s] Port [%d] connecting SYN\n", getMac(), d.getRequestingPort());
                                    portStateMap.put(d.getRequestingPort(), PortState.CONNECTING);
                                    responsePacket = new Packet<>(
                                            receivingPacket,
                                            new HandshakeData(true, true, false, receivingPacket.getSourcePort(), getMac()),
                                            d.getRequestingPort(),
                                            receivingPacket.getSourcePort()
                                    );
                                }else{
                                    System.out.printf("[%s] Port [%d] in use\n", getMac(), d.getRequestingPort());
                                    responsePacket = new Packet<>(
                                            receivingPacket,
                                            new HandshakeData(false, true, true, null, null),
                                            Integer.MAX_VALUE,
                                            receivingPacket.getSourcePort()
                                    );
                                }
                            }
                            else
                            {
                                System.out.printf("[%s] Port [%d] not open\n", getMac(), d.getRequestingPort());
//                                responsePacket = new Packet<>(
//                                        receivingPacket,
//                                        new HandshakeData(false, true, true, null, null),
//                                        Integer.MAX_VALUE,
//                                        receivingPacket.getSourcePort()
//                                );
                            }
                        }
                        case 110 -> {
                            if(portStateMap.containsKey(d.getRequestingPort()))
                            {
                                if(d.getRequestingPort() == receivingPacket.getDestinationPort() &&
                                        receivingPacket.getDestinationPort() != null &&
                                        d.getMac() != null &&
                                        portStateMap.get(d.getRequestingPort()) == PortState.CONNECTING)
                                {
                                    System.out.printf("[%s] Port [%d] connected to [%s] Port [%d]\n", getMac(), d.getRequestingPort(), d.getMac(), receivingPacket.getSourcePort());
                                    portStateMap.put(d.getRequestingPort(), PortState.CONNECTED);
                                    arpTable.put(d.getMac(), d.getRequestingPort());
                                    responsePacket = new Packet<>(
                                            receivingPacket,
                                            new HandshakeData(false, true, false, receivingPacket.getSourcePort(), getMac()),
                                            receivingPacket.getDestinationPort(),
                                            receivingPacket.getSourcePort()
                                    );
                                }else{
                                    System.out.printf("[%s] Port [%d] invalid packet SYN ACK\n", getMac(), d.getRequestingPort());
                                    responsePacket = new Packet<>(
                                            receivingPacket,
                                            new HandshakeData(false, true, true, null, null),
                                            Integer.MAX_VALUE,
                                            receivingPacket.getSourcePort()
                                    );
                                }
                            }else{
                                System.out.printf("[%s] Port [%d] not open SYN ACK\n", getMac(), d.getRequestingPort());
                            }

                        }
                        case 10 -> {
                            if(d.getRequestingPort() == receivingPacket.getDestinationPort() &&
                                    receivingPacket.getDestinationPort() != null &&
                                    portStateMap.containsKey(d.getRequestingPort()) &&
                                    d.getMac() != null &&
                                    portStateMap.get(d.getRequestingPort()) == PortState.CONNECTING){
                                System.out.printf("[%s] Port [%d] connected to [%s] Port [%d] ACK\n", getMac(), d.getRequestingPort(), d.getMac(), receivingPacket.getSourcePort());
                                arpTable.put(d.getMac(), d.getRequestingPort());
                                portStateMap.put(d.getRequestingPort(), PortState.CONNECTED);
                            }else{
                                System.out.printf("[%s] Port [%d] connection failed ACK\n", getMac(), receivingPacket.getDestinationPort());
                            }
                        }
                        case 1 -> {
                            if(receivingPacket.getDestinationPort() != null &&
                                    portStateMap.containsKey(receivingPacket.getDestinationPort()) &&
                                    d.getMac() != null &&
                                    portStateMap.get(receivingPacket.getDestinationPort()) == PortState.CONNECTED
                            )
                            {
                                if(arpTable.containsKey(d.getMac()))
                                {
                                    System.out.printf("[%s] Port [%d] disconnected \n", getMac(), receivingPacket.getDestinationPort());
                                    arpTable.remove(d.getMac());
                                }else{
                                    System.out.printf("[%s] Port [%d] connection cancelled \n", getMac(), receivingPacket.getDestinationPort());
                                }

                                portStateMap.put(receivingPacket.getDestinationPort(), PortState.OPEN);
                                portLatestPacketMap.remove(receivingPacket.getDestinationPort());
                                responsePacket = new Packet<>(
                                        receivingPacket,
                                        new HandshakeData(false, true, true, null, getMac()),
                                        receivingPacket.getDestinationPort(),
                                        receivingPacket.getSourcePort()
                                );
                            }else{
                                System.out.printf("[%s] Port [%d] invalid packet FIN \n", getMac(), receivingPacket.getDestinationPort());
                            }
                        }
                        case 11 -> {
                            if(receivingPacket.getDestinationPort() != null &&
                                    portStateMap.containsKey(receivingPacket.getDestinationPort()) &&
                                    d.getMac() != null &&
                                    arpTable.containsKey(d.getMac()) &&
                                    portStateMap.get(receivingPacket.getDestinationPort()) == PortState.CONNECTED
                            )
                            {
                                System.out.printf("[%s] Port [%d] disconnected FIN ACK\n", getMac(), receivingPacket.getDestinationPort());
                                arpTable.remove(d.getMac());
                                portStateMap.put(receivingPacket.getDestinationPort(), PortState.OPEN);
                                portLatestPacketMap.remove(receivingPacket.getDestinationPort());
                            }else if (receivingPacket.getDestinationPort() != null &&
                                    portStateMap.containsKey(receivingPacket.getDestinationPort()) &&
                                    portStateMap.get(receivingPacket.getDestinationPort()) == PortState.CONNECTING
                            ){
                                System.out.printf("[%s] Port [%d] connection failed FIN ACK\n", getMac(), receivingPacket.getDestinationPort());
                                portStateMap.put(d.getRequestingPort(), PortState.OPEN);
                            }
                            else{
                                System.out.printf("[%s] Port [%d] invalid packet FIN ACK\n", getMac(), receivingPacket.getDestinationPort());
                            }
                        }

                    }
                    if(responsePacket != null) {
                        send(responsePacket);
                    }
                }

                if(receivingQueue.isEmpty())
                    networkMode = NetworkMode.IDLE;
            }
        }
    }

    @Override
    public boolean isConnected(int port) {
        return false;
    }

    @Override
    public void connect(int sourcePort) {
        if(!openPort(sourcePort))
            return;
        System.out.printf("[%s] Port [%d] connecting\n", getMac(), sourcePort);
        portStateMap.put(sourcePort, PortState.CONNECTING);
        Packet<HandshakeData> packet = new Packet<>(
                UUID.randomUUID().toString(),
                new HandshakeData(true, false, false, sourcePort, getMac()),
                sourcePort,
                sourcePort);
        send(packet);
    }

//    @Override
//    public void handshake(boolean syn, boolean ack, boolean fin, NetworkComponent connectingComponent) {
////        send(new HandshakePacket(connectingComponent.getFreePortPair(connectingComponent)));
//
//    }

    @Override
    public void send(Packet packet) {
        sendingQueue.offer(packet);
    }

    @Override
    public void receive(Packet packet) {
        receivingQueue.offer(packet);
    }

    public void checkSessions()
    {
        List<Integer> ports = new ArrayList<>(portLatestPacketMap.keySet());
        for(int i = 0; i < ports.size(); i++)
        {
            Integer port = ports.get(i);
            if(portStateMap.getOrDefault(port, null) == PortState.CONNECTING)
            {
                Long time = System.currentTimeMillis() - portLatestPacketMap.get(port).getCreated();
                if(time > timeout)
                {
                    portStateMap.put(port, PortState.OPEN);
                    portLatestPacketMap.remove(port);
                    System.out.printf("[%s] Port [%d] timeout\n", getMac(), port);
                }
            }

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

        network.addToNetwork(component1);
        network.addToNetwork(component2);
        network.addToNetwork(component3);

        component1.connect(10);
        component1.connect(20);

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
