package aircraftsimulator.GameObject.Aircraft.Communication;

import java.util.*;

public class SessionManagerImp implements SessionManager{
    private final Map<Integer, String> portSessionMap;
    private final Map<String, SessionInformation> sessionInformationMap;
    private final Map<String, Long> sessionLastUpdatedMap;

    public SessionManagerImp() {
        portSessionMap = new HashMap<>();
        sessionInformationMap = new HashMap<>();
        sessionLastUpdatedMap = new HashMap<>();
    }

    public boolean deleteSession(String sessionId)
    {
        if(!sessionInformationMap.containsKey(sessionId))
            return false;
        Integer port = sessionInformationMap.get(sessionId).sourcePort();
        portSessionMap.remove(port);
        sessionInformationMap.remove(sessionId);
        sessionLastUpdatedMap.remove(sessionId);

        return true;
    }

    public boolean deleteSession(Integer port)
    {
        if(!portSessionMap.containsKey(port))
            return false;
        String sessionId = getSessionId(port);
        sessionInformationMap.remove(sessionId);
        sessionLastUpdatedMap.remove(sessionId);

        portSessionMap.remove(port);
        return true;
    }

    public SessionInformation getSessionInformation(String sessionId)
    {
        return sessionInformationMap.getOrDefault(sessionId, null);
    }

    public String getSessionId(Integer port)
    {
        return portSessionMap.getOrDefault(port, null);
    }

    public Map<String, SessionInformation> getSessionInformationMap(Integer port)
    {
        Map<String, SessionInformation> sessionInformationList = new HashMap<>();
        String sessionId = getSessionId(port);
        if(sessionId != null)
        {
            sessionInformationList.put(sessionId, sessionInformationMap.get(sessionId));
        }
        return sessionInformationList;
    }

    public boolean isRegistered(String sessionId, String destinationMac)
    {
        if(!sessionInformationMap.containsKey(sessionId))
            return false;
        if(sessionInformationMap.get(sessionId).destinationMac() == null)
            return true;
        return sessionInformationMap.get(sessionId).destinationMac().equals(destinationMac);
    }

    public boolean register(String sessionId, Integer sourcePort, Integer destinationPort, String destinationArp)
    {
        if(portSessionMap.containsKey(sourcePort))
            return false;
        portSessionMap.put(sourcePort, sessionId);
        sessionInformationMap.put(sessionId, new SessionInformation(sourcePort, destinationPort, destinationArp));
        sessionLastUpdatedMap.put(sessionId, System.currentTimeMillis());
        return true;
    }

    public void updateSession(String sessionId)
    {
        if(sessionLastUpdatedMap.containsKey(sessionId)) {
            sessionLastUpdatedMap.put(sessionId, System.currentTimeMillis());
        }else{
            System.out.printf("[%s] Session does not exist\n".formatted(sessionId));
        }
    }

    public void updateSession(String sessionId, Integer sourcePort, Integer destinationPort, String destinationArp)
    {
        if(sessionLastUpdatedMap.containsKey(sessionId))
        {
            sessionInformationMap.put(sessionId, new SessionInformation(sourcePort, destinationPort, destinationArp));
            sessionLastUpdatedMap.put(sessionId, System.currentTimeMillis());
        }else{
            System.out.printf("[%s] Session does not exist".formatted(sessionId));
        }
    }
}
