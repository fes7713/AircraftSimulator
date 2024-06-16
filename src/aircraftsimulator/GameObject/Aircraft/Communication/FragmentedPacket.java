package aircraftsimulator.GameObject.Aircraft.Communication;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;

public class FragmentedPacket extends Packet{

    private int sequenceNumber;
    private int ackNumber;
    private int frameSize;

    public FragmentedPacket(Packet basePacket, byte[] data)
    {
        super(basePacket, data);
    }

    public FragmentedPacket(String sessionID, SessionInformation info, HandshakeData handshakeData, byte[] data, String sourceMac) {
        super(sessionID, info, handshakeData, data, sourceMac);
    }

    public FragmentedPacket(HandshakeData handshakeData, byte[] data, @NotNull Integer sourcePort, @NotNull Integer destinationPort, String sourceMac, String destinationMac) {
        super(handshakeData, data, sourcePort, destinationPort, sourceMac, destinationMac);
    }

    public void setFragmentDetail(int sequenceNumber, int ackNumber, int frameSize)
    {
        this.sequenceNumber = sequenceNumber;
        this.ackNumber = ackNumber;
        this.frameSize = frameSize;
    }

    public Object copy(byte[] data, int sequenceNumber, int ackNumber, int frameSize)
    {
        FragmentedPacket fragmentedPacket = new FragmentedPacket(this, data);
        fragmentedPacket.setFragmentDetail(sequenceNumber, ackNumber, frameSize);
        return fragmentedPacket;
    }

    public static Queue<FragmentedPacket> fragmentPacket(String sessionId, SessionInformation info, String sourceMac, Serializable data, int frameSize)
    {
        Queue<FragmentedPacket> fragmentedPackets = new ArrayDeque<>();
        byte[][] dataArr = ByteConvertor.serialize(data, frameSize);

        FragmentedPacket basePacket = new FragmentedPacket(sessionId, info, HandshakeData.EMPTY, null, sourceMac);

        for(int i = 0; i < dataArr.length; i++)
        {
            FragmentedPacket fragmentedPacket = (FragmentedPacket) basePacket.copy(dataArr[i], i * frameSize, 0, frameSize);
            fragmentedPackets.add(fragmentedPacket);
        }
        return fragmentedPackets;
    }
    
    public static <E> E defragmentPacket(Queue<FragmentedPacket> fragmentedPackets) throws IOException, ClassNotFoundException {
        byte[][] stream = new byte[fragmentedPackets.size()][];
        for(int i = 0; fragmentedPackets.size() > 0; i++)
        {
            stream[i] = fragmentedPackets.poll().getData();
        }
        return ByteConvertor.deSerialize(stream);
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        ArrayList<PositionCommand> positionData = new ArrayList<>()
        {
            {
                add(new PositionCommand(100, "Attack"));
                add(new PositionCommand(50, "Attack"));
                add(new PositionCommand(10, "Search"));
                add(new PositionCommand(240, "Attack"));
                add(new PositionCommand(-100, "Search"));
                add(new PositionCommand(-20, "Attack"));
                add(new PositionCommand(160, "Return"));
                add(new PositionCommand(160, "Return"));
                add(new PositionCommand(160, "Return"));
                add(new PositionCommand(160, "Return"));
                add(new PositionCommand(160, "Return"));
            }
        };

        Queue<FragmentedPacket> fragmentedPackets = FragmentedPacket.fragmentPacket("Sample", new SessionInformation(10, 10, "Dest"), "Source", positionData, 128);
        System.out.println("len " + fragmentedPackets.size());
        ArrayList<PositionCommand> recreatedPositionData = FragmentedPacket.defragmentPacket(fragmentedPackets);
        for(PositionCommand positionCommand: recreatedPositionData)
            System.out.println(positionCommand);
    }
}
