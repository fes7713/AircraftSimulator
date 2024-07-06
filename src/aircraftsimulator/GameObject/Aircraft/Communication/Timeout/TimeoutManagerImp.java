package aircraftsimulator.GameObject.Aircraft.Communication.Timeout;

import aircraftsimulator.GameObject.Aircraft.Communication.Handler.Handler;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.function.BiConsumer;

public class TimeoutManagerImp implements TimeoutManager{
    private final Map<String, Map<Class<? extends Handler>, TimeoutInformation>> timeoutMap;

    public TimeoutManagerImp() {
        timeoutMap = new HashMap<>();
    }

    @Override
    public void checkTimeout() {
        for(String sessionId: new HashSet<>(timeoutMap.keySet()))
        {
            Collection<TimeoutInformation> infoSet = new HashSet<>(timeoutMap.getOrDefault(sessionId, new HashMap<>()).values());
            for(TimeoutInformation timeoutInformation: infoSet)
            {
                timeoutInformation.checkTimeout();
            }
        }
    }

    @Override
    public void registerTimeout(String sessionId, Class<? extends Handler> type, long timeout, BiConsumer<String, Integer> handler) {
        if(!timeoutMap.containsKey(sessionId))
            timeoutMap.put(sessionId, new HashMap<>());
        timeoutMap.get(sessionId).put(type, new TimeoutInformation(sessionId, timeout, handler, s -> {this.removeTimeout(s, type);}));
    }

    @Override
    public void registerTimeout(String sessionId, Class<? extends Handler> type, TimeoutInformation timeoutInformation) {
        if(!timeoutMap.containsKey(sessionId))
           timeoutMap.put(sessionId, new HashMap<>());
        timeoutMap.get(sessionId).put(type, timeoutInformation);
    }

    @Override
    public void removeTimeout(String sessionId) {
        timeoutMap.remove(sessionId);
    }

    @Override
    public void removeTimeout(String sessionId, Class<? extends Handler> type) {
        if(!timeoutMap.get(sessionId).containsKey(type))
            throw new RuntimeException("Connection timeout is not registered");
        timeoutMap.get(sessionId).remove(type);
        if(timeoutMap.get(sessionId).isEmpty())
            timeoutMap.remove(sessionId);
    }

    @Override
    public void updateTimeout(String sessionId, Class<? extends Handler> type) {
        if(!timeoutMap.get(sessionId).containsKey(type))
            throw new RuntimeException("Timeout type is not registered");
        timeoutMap.get(sessionId).get(type).resetTimeout();
    }

    @Override
    public void updateTimeout(String sessionId, Class<? extends Handler> type, BiConsumer<String, Integer> handler) {
        updateTimeout(sessionId, type);
        timeoutMap.get(sessionId).get(type).setTimeoutHandler(handler);
    }

    @Override
    public boolean isRegistered(String sessionId, Class<? extends Handler> type) {
        return timeoutMap.containsKey(sessionId) ? timeoutMap.get(sessionId).containsKey(type) : false;
    }
}
