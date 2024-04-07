package aircraftsimulator.GameObject.Aircraft.Communication;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SessionManager {
    private final Map<Integer, String> portSessionMap;
    private final Map<String, SessionInformation> sessionInformationMap;
    private final Map<String, Long> sessionLastUpdatedMap;

    private final TimeoutHandler timeoutHandler;

    public SessionManager(TimeoutHandler timeoutHandler) {
        portSessionMap = new HashMap<>();
        sessionInformationMap = new HashMap<>();
        sessionLastUpdatedMap = new HashMap<>();
        this.timeoutHandler = timeoutHandler;
    }

    public String generateSession(Integer sourcePort, Integer destinationPort, String destinationArp)
    {
        String sessionId = UUID.randomUUID().toString();
        portSessionMap.put(sourcePort, sessionId);
        sessionInformationMap.put(sessionId, new SessionInformation(sourcePort, destinationPort, destinationArp));
        sessionLastUpdatedMap.put(sessionId, System.currentTimeMillis());
        return sessionId;
    }

    public boolean deleteSession(String sessionId)
    {
        if(sessionInformationMap.containsKey(sessionId))
        {
            portSessionMap.remove(sessionInformationMap.get(sessionId).sourcePort());
            sessionInformationMap.remove(sessionId);
            sessionLastUpdatedMap.remove(sessionId);
            return true;
        }
        return false;
    }

    public boolean deleteSession(Integer port)
    {
        if(portSessionMap.containsKey(port))
        {
            sessionInformationMap.remove(portSessionMap.get(port));
            sessionLastUpdatedMap.remove(portSessionMap.get(port));
            portSessionMap.remove(port);
            return true;
        }
        return false;
    }

    public SessionInformation getSessionInformation(String sessionId)
    {
        return sessionInformationMap.getOrDefault(sessionId, null);
    }

    public String getSessionId(Integer port)
    {
        return portSessionMap.getOrDefault(port, null);
    }

    public SessionInformation getSessionInformation(Integer port)
    {
        return sessionInformationMap.getOrDefault(portSessionMap.getOrDefault(port, null), null);
    }

    public boolean isRegistred(Integer port)
    {
        return portSessionMap.containsKey(port);
    }

    public void updateSession(String sessionId)
    {
        if(sessionLastUpdatedMap.containsKey(sessionId))
            sessionLastUpdatedMap.put(sessionId, System.currentTimeMillis());
    }

    public boolean isTimeout(String sessionId, Long timeout)
    {
        if(sessionLastUpdatedMap.containsKey(sessionId))
        {
            return System.currentTimeMillis() - sessionLastUpdatedMap.get(sessionId) > timeout;
        }
        return false;
    }

    public void checkTimeout(Long timeout)
    {
        for(Integer port: portSessionMap.keySet()){
            if(isTimeout(portSessionMap.get(port), timeout))
            {
                timeoutHandler.triggerTimeout(port, sessionInformationMap.get(portSessionMap.get(port)));
            }
        }
    }
}
