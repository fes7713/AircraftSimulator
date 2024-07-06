package aircraftsimulator.GameObject.Aircraft.Communication.Handler;

import aircraftsimulator.GameObject.Aircraft.Communication.Data.AckWindowSizeData;
import aircraftsimulator.GameObject.Aircraft.Communication.Data.FragmentedData;
import aircraftsimulator.GameObject.Aircraft.Communication.Handler.NetworkError.NetworkErrorType;
import aircraftsimulator.GameObject.Aircraft.Communication.Timeout.TimeoutInformation;
import aircraftsimulator.GameObject.Aircraft.Communication.Timeout.TimeoutManager;

public class ContinuousFragmentHandler extends FragmentHandler{

    public ContinuousFragmentHandler(long resendTimeout, TimeoutManager timeoutManager, FragmentAdaptor adaptor)
    {
        super(resendTimeout, timeoutManager, adaptor);
    }

    protected void handleAckWindowSizeData(AckWindowSizeData data, int port) {
        String sessionId = adaptor.getSessionId(port);
        if(fragmentStoreMap.get(sessionId) == null)
        {
            if(timeoutManager.isRegistered(sessionId, FragmentHandler.class))
                timeoutManager.removeTimeout(sessionId, FragmentHandler.class);
            return;
        }

        int lastSent = fragmentLastSentMap.getOrDefault(sessionId, -1);
        int ackedTill = progressMap.get(sessionId);
        byte[][] fragmentArr = fragmentStoreMap.get(sessionId);

        if(ackedTill > data.ackNumber())
            return;

        progressMap.put(sessionId, data.ackNumber() - 1);

        if(windowChangePointMap.get(sessionId) <= data.ackNumber())
        {
            windowSizeMap.put(sessionId, Math.min(windowSizeMap.get(sessionId) * 2, data.windowSize()));
            int windowsSize = windowSizeMap.get(sessionId);
            int lastIndex = Math.min(fragmentArr.length, lastSent + windowsSize + 1);
            windowChangePointMap.put(sessionId, lastIndex);
            fragmentLastSentMap.put(sessionId, lastIndex - 1);
            for(int i = lastSent + 1; i < lastIndex; i++)
                adaptor.serializableDataSend(sessionId, new FragmentedData(fragmentArr[i], i, 0, windowsSize, fragmentArr.length));
            if(fragmentArr.length == lastIndex && lastIndex != lastSent + 1)
            {
                fragmentSendCompletionHandler(port);
                return;
            }
        }else{
            // Already Acked data, no sending
            if(ackedTill  >= data.ackNumber() - 1)
            {
                return;
            }
            else{
                // Continouse Sending
                if(lastSent + 1 < fragmentArr.length)
                {
                    fragmentLastSentMap.put(sessionId, lastSent + 1);
                    adaptor.serializableDataSend(sessionId, new FragmentedData(fragmentArr[lastSent + 1], lastSent + 1, 0, windowSizeMap.get(sessionId), fragmentArr.length));
                }else{
                    fragmentSendCompletionHandler(port);
                    return;
                }
            }
        }

        if(fragmentArr.length == data.ackNumber())
        {
            fragmentSendAckCompletionHandler(port);
            return;
        }

        timeoutManager.registerTimeout(sessionId, FragmentHandler.class,
                new TimeoutInformation(sessionId, resendTimeout, resendTimeout, 0, 1, 3,
                        (s, integer) -> {
                            int askedTill = progressMap.get(sessionId);
                            windowSizeMap.put(s, 1);
                            windowChangePointMap.put(s, askedTill + 1 + 1);
                            fragmentLastSentMap.put(s, askedTill + 1);
                            adaptor.serializableDataSend(sessionId, new FragmentedData(fragmentArr[askedTill + 1], askedTill + 1, 0, 1, fragmentArr.length));
                        },
                        s -> {
                            fragmentSendAckCompletionHandler(port);
                            adaptor.errorHandler(s, NetworkErrorType.TIMEOUT);
                        }
                )
        );
    };
}
