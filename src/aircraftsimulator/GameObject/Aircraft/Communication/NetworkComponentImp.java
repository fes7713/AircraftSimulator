package aircraftsimulator.GameObject.Aircraft.Communication;

import java.io.IOException;
import java.util.*;

public class NetworkComponentImp implements NetworkComponent{
    private final Network network;
    private final String mac;

    private final Map<Integer, PortState> portStateMap;
    private final Map<String, Integer> arpTable;
    private final Map<Integer, Packet<?>> portLastSentPacketMap;

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
        portLastSentPacketMap = new HashMap<>();

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
                System.out.printf("[%6s-%6s] Port [%d] already open\n", getMac().substring(0, 6), "", port);
                return true;
            }
            case CONNECTED, CONNECTING -> {
                System.out.printf("[%6s-%6s] Port [%d] in use\n", getMac().substring(0, 6), "", port);
                return false;
            }
            case CLOSED ->{
                System.out.printf("[%6s-%6s] Port [%d] opened\n", getMac().substring(0, 6), "", port);
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
                System.out.printf("[%6s-%6s] Open Port [%d] closed\n", getMac(), "", port);
                portStateMap.remove(port);
                portLastSentPacketMap.remove(port);
            }
            case CONNECTED, CONNECTING -> {
                System.out.printf("[%6s-%6s] Port [%d] disconnecting\n", getMac(), "", port);
                send(
                        new Packet<>(
                                portLastSentPacketMap.get(port),
                                new HandshakeData(false, false, false, true),
                                portLastSentPacketMap.get(port).getSourcePort(),
                                portLastSentPacketMap.get(port).getDestinationPort(),
                                portLastSentPacketMap.get(port).getSourceMac(),
                                portLastSentPacketMap.get(port).getDestinationMac()

                        )
                );
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
                boolean newSessionFlag = false;

                if(!portLastSentPacketMap.containsKey(sendingPacket.getSourcePort()))
                {
                    if(!(sendingPacket.getData() instanceof HandshakeData hd && hd.isAck()))
                    {
                        portLastSentPacketMap.put(sendingPacket.getSourcePort(), sendingPacket);
                        newSessionFlag = true;
                    }
                }

                String destinationMac = sendingPacket.getDestinationMac();
                if(destinationMac != null && newSessionFlag)
                    throw new RuntimeException("Invalid state of program");

                if(destinationMac != null)
                    network.sendTo(destinationMac, sendingPacket);
                else
                {

                    network.broadcast(sendingPacket, getMac());
                }

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
                        return;
                    }
                    switch (code)
                    {
                        // SYN
                        case 1000 -> {
                            if(portStateMap.get(receivingPacket.getDestinationPort()) == PortState.OPEN)
                            {
                                System.out.printf("[%6s-%6s] Port [%d] connecting SYN\n", getMac().substring(0, 6), receivingPacket.getSourceMac().substring(0, 6), receivingPacket.getDestinationPort());
                                portStateMap.put(receivingPacket.getDestinationPort(), PortState.CONNECTING);
                                responsePacket = new Packet<>(
                                        receivingPacket,
                                        new HandshakeData(true, true, false, false),
                                        getMac()
                                );
                            }else{
                                System.out.printf("[%6s-%6s] Port [%d] in use\n", getMac().substring(0, 6), receivingPacket.getSourceMac().substring(0, 6), receivingPacket.getDestinationPort());
                            }
                        }
                        case 1100 -> {
                            if(portStateMap.get(receivingPacket.getDestinationPort()) == PortState.CONNECTING)
                            {
                                System.out.printf("[%6s-%6s] Port [%d] connected to [%s] Port [%d]\n", getMac().substring(0, 6), receivingPacket.getSourceMac().substring(0, 6), receivingPacket.getDestinationPort(), receivingPacket.getSourceMac(), receivingPacket.getSourcePort());
                                portStateMap.put(receivingPacket.getDestinationPort(), PortState.CONNECTED);
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
                            }

                        }
                        case 100 -> {
                            if(portStateMap.get(receivingPacket.getDestinationPort()) == PortState.CONNECTING){
                                System.out.printf("[%6s-%6s] Port [%d] connected to [%s] Port [%d] ACK\n", getMac().substring(0, 6), receivingPacket.getSourceMac().substring(0, 6), receivingPacket.getDestinationPort(), receivingPacket.getSourceMac(), receivingPacket.getSourcePort());
                                registerArp(receivingPacket);
                                portStateMap.put(receivingPacket.getDestinationPort(), PortState.CONNECTED);
                            }else{
                                System.out.printf("[%6s-%6s] Port [%d] connection failed ACK\n", getMac().substring(0, 6), receivingPacket.getSourceMac().substring(0, 6), receivingPacket.getDestinationPort());
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

                                portStateMap.put(receivingPacket.getDestinationPort(), PortState.OPEN);
                                portLastSentPacketMap.remove(receivingPacket.getDestinationPort());
                                responsePacket = new Packet<>(
                                        receivingPacket,
                                        new HandshakeData(false, true, false, true),
                                        getMac()
                                );
                            }else{
                                System.out.printf("[%6s-%6s] Port [%d] invalid packet FIN \n", getMac().substring(0, 6), receivingPacket.getSourceMac().substring(0, 6), receivingPacket.getDestinationPort());
                            }
                        }
                        case 101 -> {
                            if(arpTable.containsKey(receivingPacket.getSourceMac()) &&
                                    portStateMap.get(receivingPacket.getDestinationPort()) == PortState.CONNECTED
                            )
                            {
                                System.out.printf("[%6s-%6s] Port [%d] disconnected FIN ACK\n", getMac().substring(0, 6), receivingPacket.getSourceMac().substring(0, 6), receivingPacket.getDestinationPort());
                                arpTable.remove(receivingPacket.getSourceMac());
                                portStateMap.put(receivingPacket.getDestinationPort(), PortState.OPEN);
                                portLastSentPacketMap.remove(receivingPacket.getDestinationPort());
                            }else if (portStateMap.get(receivingPacket.getDestinationPort()) == PortState.CONNECTING
                            ){
                                System.out.printf("[%6s-%6s] Port [%d] connection failed FIN ACK\n", getMac().substring(0, 6), receivingPacket.getSourceMac().substring(0, 6), receivingPacket.getDestinationPort());
                                portStateMap.put(receivingPacket.getDestinationPort(), PortState.OPEN);
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

    @Override
    public boolean isConnected(int port) {
        return false;
    }

    @Override
    public void connect(int sourcePort) {
        if(!openPort(sourcePort))
            return;

        portStateMap.put(sourcePort, PortState.CONNECTING);
        Packet<HandshakeData> packet = new Packet<>(
                UUID.randomUUID().toString(),
                new HandshakeData(true, false, false, false),
                sourcePort,
                sourcePort,
                getMac(),
                null);
        System.out.printf("[%6s-%6s] Port [%d] connecting\n", getMac().substring(0, 6), "", sourcePort);
        send(packet);
    }

//    @Override
//    public void handshake(boolean syn, boolean ack, boolean fin, NetworkComponent connectingComponent) {
////        send(new HandshakePacket(connectingComponent.getFreePortPair(connectingComponent)));
//
//    }

    @Override
    public void send(Packet<?> packet) {
        sendingQueue.offer(packet);
    }

    @Override
    public void receive(Packet<?> packet) {
        receivingQueue.offer(packet);
    }

    public void checkSessions()
    {
        List<Integer> ports = new ArrayList<>(portLastSentPacketMap.keySet());
        for(int i = 0; i < ports.size(); i++)
        {
            Integer port = ports.get(i);
            if(portStateMap.getOrDefault(port, null) == PortState.CONNECTING)
            {
                Long time = System.currentTimeMillis() - portLastSentPacketMap.get(port).getCreated();
                if(time > timeout)
                {
                    portStateMap.put(port, PortState.OPEN);
                    portLastSentPacketMap.remove(port);
                    System.out.printf("[%6s-] Port [%d] timeout\n", getMac().substring(0, 6), port);
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
