package aircraftsimulator.GameObject.Aircraft.Communication;

import aircraftsimulator.GameObject.Aircraft.Communication.Data.FragmentedData;

import java.io.IOException;

public interface FragmentHandler extends DataReceiver<FragmentedData>{
    void startStoringFragments(FragmentedData data, String sessionId);
    byte[][] getFragmentArr(String sessionId);
    void fragmentCompletionHandler(Object object, String sessionId);

    default boolean verifyFragmentComplete(String sessionId){
        byte[][] fragmentArr = getFragmentArr(sessionId);
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < fragmentArr.length; i++)
            sb.append(fragmentArr[i] == null ? "[ ]" : "[v]");
        System.out.println(sb);
        for(int i = 0; i < fragmentArr.length; i++)
            if(fragmentArr[i] == null)
                return false;
        return true;
    }

    @Override
    default void dataReceived(FragmentedData data, String sessionId){
        byte[][] fragmentArr = getFragmentArr(sessionId);
        if(fragmentArr == null)
        {
            startStoringFragments(data, sessionId);
            fragmentArr = getFragmentArr(sessionId);
        }
        fragmentArr[data.sequenceNumber()] = data.fragmentedData();

        if(verifyFragmentComplete(sessionId))
        {
            try {
                fragmentCompletionHandler(ByteConvertor.deSerialize(fragmentArr), sessionId);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
