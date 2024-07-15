package aircraftsimulator.GameObject.Aircraft.Communication.Handler;

import aircraftsimulator.GameObject.Aircraft.Communication.ByteConvertor;
import aircraftsimulator.GameObject.Aircraft.Communication.Data.AckWindowSizeData;
import aircraftsimulator.GameObject.Aircraft.Communication.Data.FragmentedData;
import aircraftsimulator.GameObject.Aircraft.Communication.Data.RequestWindowSize;
import aircraftsimulator.GameObject.Aircraft.Communication.Logger.Logger;
import aircraftsimulator.GameObject.Aircraft.Communication.Handler.NetworkError.NetworkErrorType;
import aircraftsimulator.GameObject.Aircraft.Communication.Logger.ProgressLogger;
import aircraftsimulator.GameObject.Aircraft.Communication.Timeout.TimeoutInformation;
import aircraftsimulator.GameObject.Aircraft.Communication.Timeout.TimeoutManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FragmentHandler implements Handler{
    protected final Map<String, byte[][]> fragmentStoreMap;
    protected final Map<String, Integer> fragmentLastSentMap;
    protected final Map<String, Integer> windowSizeMap;
    protected final Map<String, Integer> progressMap;
    protected final Map<String, Integer> windowChangePointMap;

    protected final TimeoutManager timeoutManager;
    protected final FragmentAdaptor adaptor;

    protected final long resendTimeout;

    public FragmentHandler(long resendTimeout, TimeoutManager timeoutManager, FragmentAdaptor adaptor)
    {
        this.resendTimeout = resendTimeout;
        this.timeoutManager = timeoutManager;
        this.adaptor = adaptor;

        fragmentStoreMap = new HashMap<>();
        fragmentLastSentMap = new HashMap<>();
        windowSizeMap = new HashMap<>();
        progressMap = new HashMap<>();
        windowChangePointMap = new HashMap<>();

        initHandler();
    }

    public void initHandler()
    {
        adaptor.addDataReceiver(RequestWindowSize.class, this::handleRequestWindowSize);
        adaptor.addDataReceiver(FragmentedData.class, this::handleFragmentData);
        adaptor.addDataReceiver(AckWindowSizeData.class, this::handleAckWindowSizeData);
    }

    public boolean isIdle(int port)
    {
        return !fragmentStoreMap.containsKey(adaptor.getSessionId(port));
    }

    public void sendData(int port, byte[][] stream) {
        String sessionId = adaptor.getSessionId(port);
        fragmentStoreMap.put(sessionId, stream);
        windowSizeMap.put(sessionId, 1);
        progressMap.put(sessionId, -1);
        fragmentLastSentMap.put(sessionId, -1);
        windowChangePointMap.put(sessionId, 0);
        adaptor.serializableDataSend(sessionId, new RequestWindowSize(stream.length));

        timeoutManager.registerTimeout(sessionId, FragmentHandler.class,
                new TimeoutInformation(sessionId, resendTimeout, resendTimeout, 0, 1, 3,
                        ((s, integer) -> {
                            adaptor.serializableDataSend(sessionId, new RequestWindowSize(stream.length));
                        }
                        ),
                        s -> {
                            fragmentSendAckCompletionHandler(port);
                            adaptor.errorHandler(s, NetworkErrorType.TIMEOUT);
                        }
                ));
    }

    protected void handleFragmentData(FragmentedData data, int port) {
        String sessionId = adaptor.getSessionId(port);
        if(!progressMap.containsKey(sessionId))
        {
            adaptor.serializableDataSend(sessionId, new AckWindowSizeData(data.totalFrames(), adaptor.askForWindowSize()));
            return;
        }
        int waitingFragment = progressMap.get(sessionId);
        byte[][] fragmentArr = fragmentStoreMap.get(sessionId);

        if(waitingFragment > data.sequenceNumber())
            return;

        if(waitingFragment < data.sequenceNumber())
        {
            adaptor.serializableDataSend(sessionId, new AckWindowSizeData(waitingFragment, adaptor.askForWindowSize()));
            return;
        }

        fragmentArr[waitingFragment] = data.fragmentedData();
//        StringBuilder sb = new StringBuilder();
//        for(int i = 0; i < fragmentArr.length; i++)
//            sb.append(fragmentArr[i] != null ? "[v]":"[ ]");
//        System.out.println(sb);
        ProgressLogger.PutProgressData(sessionId, waitingFragment + 1);
        ProgressLogger.PrintProgress(sessionId);
        progressMap.put(sessionId, waitingFragment + 1);
        adaptor.serializableDataSend(sessionId, new AckWindowSizeData(waitingFragment + 1, adaptor.askForWindowSize()));
        if(waitingFragment == fragmentArr.length - 1) {
            try {
                fragmentReceiveCompletionHandler(ByteConvertor.deSerialize(fragmentArr), port);
            } catch (IOException e) {
                e.printStackTrace();
                adaptor.errorHandler(sessionId, NetworkErrorType.DATA_CORRUPTED);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                adaptor.errorHandler(sessionId, NetworkErrorType.DATA_CORRUPTED);
            }
        }
    };

    protected void handleRequestWindowSize(RequestWindowSize data, int port) {
        String sessionId = adaptor.getSessionId(port);
        fragmentStoreMap.put(sessionId, new byte[data.totalFrameSize()][]);
        adaptor.serializableDataSend(sessionId, new AckWindowSizeData(0, adaptor.askForWindowSize()));
        progressMap.put(sessionId, 0);
    };

    protected void handleAckWindowSizeData(AckWindowSizeData data, int port) {
        String sessionId = adaptor.getSessionId(port);
        if(fragmentStoreMap.get(sessionId) == null)
        {
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
//                                for(int i = askedTill + 1; i <= last; i++)
//                                    serializableDataSend(sessionId, new FragmentedData(fragmentArr[i], i, 0, 1, fragmentArr.length));
                        },
                        s -> {
                            fragmentSendAckCompletionHandler(port);
                            adaptor.errorHandler(s, NetworkErrorType.TIMEOUT);
                        }
                )
        );
    };

    protected void fragmentReceiveCompletionHandler(Object object, int port) {
        String sessionId = adaptor.getSessionId(port);
        fragmentStoreMap.remove(sessionId);
        ProgressLogger.PrintProgress(sessionId);
        System.out.print("\n");
        ProgressLogger.RemoveProgressData(sessionId);
        adaptor.triggerReceiver(sessionId, object);
        progressMap.remove(sessionId);
    }

    protected void fragmentSendCompletionHandler(int port) {
        Logger.Log(Logger.LogLevel.DEBUG, "Data Send Complete", "", port);
    }

    protected void fragmentSendAckCompletionHandler(int port)
    {
        String sessionId = adaptor.getSessionId(port);
        Logger.Log(Logger.LogLevel.DEBUG, "Data Send Complete ACK", "", port);
        fragmentStoreMap.remove(sessionId);
        timeoutManager.removeTimeout(sessionId, FragmentHandler.class);
        windowSizeMap.remove(sessionId);
        progressMap.remove(sessionId);
        fragmentLastSentMap.remove(sessionId);
        adaptor.sendCompletionHandler(sessionId);
    }


}
