package aircraftsimulator.GameObject.Aircraft.Communication.Timeout;

import aircraftsimulator.GameObject.Aircraft.Communication.Handler.Handler;

import java.util.function.BiConsumer;

public interface TimeoutManager {
    void checkTimeout();
    void registerTimeout(String sessionId, Class<? extends Handler> type, long timeout, BiConsumer<String, Integer> handler);
    void registerTimeout(String sessionId, Class<? extends Handler> type, TimeoutInformation timeoutInformation);
    void removeTimeout(String sessionId);
    void removeTimeout(String sessionId, Class<? extends Handler> type);
    void updateTimeout(String sessionId, Class<? extends Handler> type);
    void updateTimeout(String sessionId, Class<? extends Handler> type, BiConsumer<String, Integer> handler);
}
