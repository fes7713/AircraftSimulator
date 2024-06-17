package aircraftsimulator.GameObject.Aircraft.Communication.Data;

import aircraftsimulator.GameObject.Aircraft.Communication.ByteConvertor;

import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Objects;
import java.util.Queue;

public final class FragmentedData implements Data {
    public static final int FIXED_PRE_SIZE =
            ByteConvertor.serialize(new FragmentedData(new byte[0], 0, 0, 0, 0)).length;
    @Serial
    private static final long serialVersionUID = 0L;
    private final byte[] fragmentedData;
    private final int sequenceNumber;
    private int ackNumber;
    private int windowSize;
    private final int totalFrames;

    public FragmentedData(byte[] fragmentedData, int sequenceNumber, int ackNumber, int windowSize,
                          int totalFrames) {
        this.fragmentedData = fragmentedData;
        this.sequenceNumber = sequenceNumber;
        this.ackNumber = ackNumber;
        this.windowSize = windowSize;
        this.totalFrames = totalFrames;
    }

    public static Queue<FragmentedData> fragmentPacket(Serializable data, int frameSize) {
        Queue<FragmentedData> fragmentedData = new ArrayDeque<>();
        byte[][] dataArr = ByteConvertor.serialize(data, frameSize);

        for (int i = 0; i < dataArr.length; i++) {
            fragmentedData.add(new FragmentedData(dataArr[i], i, 0, 0, dataArr.length));
        }
        return fragmentedData;
    }

    public static <E> E defragmentPacket(Queue<FragmentedData> fragmentedData) throws IOException, ClassNotFoundException {
        byte[][] stream = new byte[fragmentedData.size()][];
        for (int i = 0; fragmentedData.size() > 0; i++) {
            stream[i] = fragmentedData.poll().fragmentedData();
        }
        return ByteConvertor.deSerialize(stream);
    }

    public byte[] fragmentedData() {
        return fragmentedData;
    }

    public int sequenceNumber() {
        return sequenceNumber;
    }

    public int ackNumber() {
        return ackNumber;
    }

    public int windowSize() {
        return windowSize;
    }

    public int totalFrames() {
        return totalFrames;
    }

    public void setAckNumber(int ackNumber) {
        this.ackNumber = ackNumber;
    }

    public void setWindowSize(int windowSize) {
        this.windowSize = windowSize;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (FragmentedData) obj;
        return Objects.equals(this.fragmentedData, that.fragmentedData) &&
                this.sequenceNumber == that.sequenceNumber &&
                this.ackNumber == that.ackNumber &&
                this.windowSize == that.windowSize &&
                this.totalFrames == that.totalFrames;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fragmentedData, sequenceNumber, ackNumber, windowSize, totalFrames);
    }

    @Override
    public String toString() {
        return "FragmentedData[" +
                "fragmentedData=" + fragmentedData + ", " +
                "sequenceNumber=" + sequenceNumber + ", " +
                "ackNumber=" + ackNumber + ", " +
                "windowSize=" + windowSize + ", " +
                "totalFrames=" + totalFrames + ']';
    }


//    public static void main(String[] args) throws IOException, ClassNotFoundException {
//        ArrayList<PositionCommand> positionData = new ArrayList<>()
//        {
//            {
//                add(new PositionCommand(100, "Attack"));
//                add(new PositionCommand(50, "Attack"));
//                add(new PositionCommand(10, "Search"));
//                add(new PositionCommand(240, "Attack"));
//                add(new PositionCommand(-100, "Search"));
//                add(new PositionCommand(-20, "Attack"));
//                add(new PositionCommand(160, "Return"));
//                add(new PositionCommand(160, "Return"));
//                add(new PositionCommand(160, "Return"));
//                add(new PositionCommand(160, "Return"));
//                add(new PositionCommand(160, "Return"));
//            }
//        };
//
//        Queue<FragmentedData> fragmentedData = FragmentedData.fragmentPacket(positionData, 128);
//        System.out.println("len " + fragmentedData.size());
//        ArrayList<PositionCommand> recreatedPositionData = FragmentedData.defragmentPacket(fragmentedData);
//        for(PositionCommand positionCommand: recreatedPositionData)
//            System.out.println(positionCommand);
//    }
}
