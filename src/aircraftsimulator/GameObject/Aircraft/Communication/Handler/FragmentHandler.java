package aircraftsimulator.GameObject.Aircraft.Communication.Handler;

import aircraftsimulator.GameObject.Aircraft.Communication.ByteConvertor;
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

    default void printFragmentProgress(String sessionId) {
        byte[][] fragmentArr = getFragmentArr(sessionId);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fragmentArr.length; i++)
            sb.append(fragmentArr[i] == null ? "[ ]" : "[v]");
        System.out.println(sb);
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
            for(int i = 0; i < data.ackNumber(); i++)
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

        printFragmentProgress(sessionId);

        if(isFragmentLost(sessionId))
            return;

        if(data.sequenceNumber() == data.ackNumber() + data.windowSize() - 1 || data.sequenceNumber() == data.totalFrames() - 1)
        {
            serializableDataSend(sessionId, new AckWindowSizeData(data.sequenceNumber() + 1, askForWindowSize()));
        }

        if(fragmentArr[fragmentArr.length - 1] != null)
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

    default boolean isFragmentLost(String sessionId)
    {
        byte[][] fragmentArr = getFragmentArr(sessionId);
        int end = fragmentArr.length - 1;
        int start = -1;
        for(int i = 0; i < fragmentArr.length; i++)
            if(fragmentArr[i] != null)
                start = i;
            else
                break;

        for(int i = fragmentArr.length - 1; i >= 0; i--)
            if(fragmentArr[i] == null)
                end = i;
            else
                break;

        if(end - start == 1) {
        }
        else if(start != end) {
            if(fragmentArr[end - 2] == null)
                fragmentLossHandler(start + 1, sessionId);
            return true;
        }
        return false;
    }

    default void fragmentLossHandler(int lostFrame, String sessionId)
    {
        serializableDataSend(sessionId, new AckWindowSizeData(lostFrame, askForWindowSize()));
    }

    default void requestLatestFrame(String sessionId)
    {
        byte[][] fragmentArr = getFragmentArr(sessionId);
        int index = -1;
        for(int i = 0; i < fragmentArr.length; i++)
            if(fragmentArr[i] != null)
                index = i;
            else
                break;
        if(index != fragmentArr.length - 1)
            serializableDataSend(sessionId, new AckWindowSizeData(index + 1, askForWindowSize()));
    }
}
