package aircraftsimulator.GameObject.Aircraft.Communication;

import aircraftsimulator.GameObject.Aircraft.Communication.Data.AckWindowSizeData;
import aircraftsimulator.GameObject.Aircraft.Communication.Data.FragmentedData;
import aircraftsimulator.GameObject.Aircraft.Communication.Data.RequestWindowSize;

import java.io.IOException;
import java.io.Serializable;

public interface FragmentHandler {
    void startStoringFragments(int totalFrames, String sessionId);

    byte[][] getFragmentArr(String sessionId);

    void fragmentReceiveCompletionHandler(Object object, String sessionId);

    void fragmentSendCompletionHandler(String sessionId);

    void serializableDataSend(String sessionId, Serializable data);

    int askForWindowSize();

    default boolean verifyFragmentComplete(String sessionId) {
        byte[][] fragmentArr = getFragmentArr(sessionId);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fragmentArr.length; i++)
            sb.append(fragmentArr[i] == null ? "[ ]" : "[v]");
        System.out.println(sb);
        for (int i = 0; i < fragmentArr.length; i++)
            if (fragmentArr[i] == null)
                return false;
        return true;
    }

    default void ackWindowDataReceived(AckWindowSizeData data, String sessionId) {
        if (getFragmentArr(sessionId) == null)
            System.out.println("Fragment Queue is not set");
        else {

            if (data.ackNumber() == getFragmentArr(sessionId).length) {
                System.out.println("Data send complete [ACK]");
                fragmentSendCompletionHandler(sessionId);
                return;
            }

            byte[][] arrData = getFragmentArr(sessionId);
            if (arrData[data.ackNumber()] == null) {
                System.out.println("Invalid Ack number");
                return;
            }
            for(int i = 0; i < data.ackNumber() - 1; i++)
                arrData[i] = null;

            for (int i = 0; i < data.windowSize() && data.ackNumber() + i < arrData.length; i++) {
                FragmentedData fragData = new FragmentedData(
                        arrData[data.ackNumber() + i],
                        data.ackNumber() + i,
                        data.ackNumber(),
                        data.windowSize(),
                        arrData.length
                    );
                serializableDataSend(sessionId, fragData);
            }
            if(data.ackNumber() + data.windowSize() >= arrData.length)
                System.out.println("Data send complete");
        }
    }

    default void requestWindowSizeDataReceiver(RequestWindowSize data, String sessionId){
        startStoringFragments(data.totalFrameSize(), sessionId);
        serializableDataSend(sessionId, new AckWindowSizeData(0, askForWindowSize()));
    }

    default void fragmentDataReceiver(FragmentedData data, String sessionId)
    {
        byte[][] fragmentArr = getFragmentArr(sessionId);
        if(fragmentArr == null)
        {
            startStoringFragments(data.totalFrames(), sessionId);
            fragmentArr = getFragmentArr(sessionId);
        }
        fragmentArr[data.sequenceNumber()] = data.fragmentedData();
        if(data.sequenceNumber() == data.ackNumber() + data.windowSize() - 1 || data.sequenceNumber() == data.totalFrames() - 1)
        {
            serializableDataSend(sessionId, new AckWindowSizeData(data.sequenceNumber() + 1, askForWindowSize()));
        }

        if(verifyFragmentComplete(sessionId))
        {
            try {
                fragmentReceiveCompletionHandler(ByteConvertor.deSerialize(fragmentArr), sessionId);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
