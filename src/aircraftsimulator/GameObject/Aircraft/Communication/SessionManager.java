package aircraftsimulator.GameObject.Aircraft.Communication;

import java.util.*;

public class SessionManager {
    private final Map<Integer, List<String>> portSessionMap;
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
        register(sessionId, sourcePort, destinationPort, destinationArp);
        return sessionId;
    }

    public boolean deleteSession(String sessionId)
    {
        if(!sessionInformationMap.containsKey(sessionId))
        {
            return false;
        }
        Integer port = sessionInformationMap.get(sessionId).sourcePort();

        portSessionMap.get(port).remove(sessionId);
        sessionInformationMap.remove(sessionId);
        sessionLastUpdatedMap.remove(sessionId);

        return true;
    }

    public boolean deleteSession(Integer port)
    {
        if(!portSessionMap.containsKey(port))
            return false;
        for(String sessionId: new ArrayList<>(getSessionId(port)))
        {
            if(portSessionMap.containsKey(port))
            {
                sessionInformationMap.remove(sessionId);
                sessionLastUpdatedMap.remove(sessionId);
            }
        }
        portSessionMap.remove(port);
        return true;
    }

    public SessionInformation getSessionInformation(String sessionId)
    {
        return sessionInformationMap.getOrDefault(sessionId, null);
    }

    public List<String> getSessionId(Integer port)
    {
        return portSessionMap.getOrDefault(port, new ArrayList<>());
    }

    public Map<String, SessionInformation> getSessionInformationMap(Integer port)
    {
        Map<String, SessionInformation> sessionInformationList = new HashMap<>();
        for(String sessionId: getSessionId(port))
        {
            sessionInformationList.put(sessionId, sessionInformationMap.get(sessionId));
        }
        return sessionInformationList;
    }

    public boolean isRegistered(Integer port)
    {
        return portSessionMap.containsKey(port);
    }

    public boolean isRegistered(String sessionId)
    {
        return sessionInformationMap.containsKey(sessionId);
    }

    public void register(String sessionId, Integer sourcePort, Integer destinationPort, String destinationArp)
    {
        if(!portSessionMap.containsKey(sourcePort))
            portSessionMap.put(sourcePort, new ArrayList<>());
        portSessionMap.get(sourcePort).add(sessionId);
        sessionInformationMap.put(sessionId, new SessionInformation(sourcePort, destinationPort, destinationArp));
        sessionLastUpdatedMap.put(sessionId, System.currentTimeMillis());
    }

    public void updateSession(String sessionId, Integer sourcePort, Integer destinationPort, String destinationArp)
    {
        if(sessionLastUpdatedMap.containsKey(sessionId))
        {
            sessionInformationMap.put(sessionId, new SessionInformation(sourcePort, destinationPort, destinationArp));
            sessionLastUpdatedMap.put(sessionId, System.currentTimeMillis());
        }else{
            System.out.printf("");
        }
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
        for(Integer port: portSessionMap.keySet())
        {
            for(String sessionId: getSessionId(port))
            {
                if(isTimeout(sessionId, timeout))
                {
                    timeoutHandler.triggerTimeout(port, sessionInformationMap.get(sessionId));
                }
            }
        }
    }
}
