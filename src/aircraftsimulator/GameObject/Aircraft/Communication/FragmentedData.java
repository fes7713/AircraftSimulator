package aircraftsimulator.GameObject.Aircraft.Communication;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;

public class FragmentedData{

    private byte[] fragmentedData;
    private int sequenceNumber;
    private int ackNumber;
    private int frameSize;
    private int totalFrames;

    public FragmentedData(byte[] data, int sequenceNumber, int ackNumber, int frameSize, int totalFrames)
    {
        this.fragmentedData = data;
        this.sequenceNumber = sequenceNumber;
        this.ackNumber = ackNumber;
        this.frameSize = frameSize;
        this.totalFrames = totalFrames;
    }

    public byte[] getFragmentedData()
    {
        return fragmentedData;
    }

    public static Queue<FragmentedData> fragmentPacket(Serializable data, int frameSize)
    {
        Queue<FragmentedData> fragmentedData = new ArrayDeque<>();
        byte[][] dataArr = ByteConvertor.serialize(data, frameSize);

        for(int i = 0; i < dataArr.length; i++)
        {
            fragmentedData.add(new FragmentedData(dataArr[i], frameSize * i, 0, frameSize, dataArr.length));
        }
        return fragmentedData;
    }
    
    public static <E> E defragmentPacket(Queue<FragmentedData> fragmentedData) throws IOException, ClassNotFoundException {
        byte[][] stream = new byte[fragmentedData.size()][];
        for(int i = 0; fragmentedData.size() > 0; i++)
        {
            stream[i] = fragmentedData.poll().getFragmentedData();
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

        Queue<FragmentedData> fragmentedData = FragmentedData.fragmentPacket(positionData, 128);
        System.out.println("len " + fragmentedData.size());
        ArrayList<PositionCommand> recreatedPositionData = FragmentedData.defragmentPacket(fragmentedData);
        for(PositionCommand positionCommand: recreatedPositionData)
            System.out.println(positionCommand);
    }
}
