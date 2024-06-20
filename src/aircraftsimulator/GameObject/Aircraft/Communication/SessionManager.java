package aircraftsimulator.GameObject.Aircraft.Communication;

import java.util.*;

public interface SessionManager {

    default String generateSession(Integer sourcePort, Integer destinationPort, String destinationArp)
    {
        String sessionId = UUID.randomUUID().toString();
        boolean result = register(sessionId, sourcePort, destinationPort, destinationArp);
        if(result)
            return sessionId;
        else
            return null;
    }
    boolean deleteSession(String sessionId);
    boolean deleteSession(Integer port);

    SessionInformation getSessionInformation(String sessionId);
    String getSessionId(Integer port);

    boolean isRegistered(String sessionId, String destinationMac);
     boolean register(String sessionId, Integer sourcePort, Integer destinationPort, String destinationArp);
    void updateSession(String sessionId);
    void updateSession(String sessionId, Integer sourcePort, Integer destinationPort, String destinationArp);

    boolean isTimeout(String sessionId, Long timeout);
    void checkTimeout(Long timeout);
}
