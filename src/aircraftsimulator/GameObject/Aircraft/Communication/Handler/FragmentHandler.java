package aircraftsimulator.GameObject.Aircraft.Communication.Handler;

import java.io.Serializable;

public interface FragmentHandler extends Handler{
    void startStoringFragments(int totalFrames, String sessionId);

    void fragmentReceiveCompletionHandler(Object object, String sessionId);

    void fragmentSendCompletionHandler(String sessionId);

    void serializableDataSend(String sessionId, Serializable data);

    int askForWindowSize();
}
