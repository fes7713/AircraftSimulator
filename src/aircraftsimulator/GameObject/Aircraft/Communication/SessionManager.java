package aircraftsimulator.GameObject.Aircraft.Communication;

import java.util.*;

public interface SessionManager {

    default String generateSession()
    {
        return UUID.randomUUID().toString();
    }
    boolean deleteSession(String sessionId);
    boolean deleteSession(Integer port);

    SessionInformation getSessionInformation(String sessionId);
    String getSessionId(Integer port);
    Integer getPort(String sessionId);

    boolean isRegistered(String sessionId, String destinationMac);
     boolean register(String sessionId, Integer sourcePort, Integer destinationPort, String destinationArp);
    void updateSession(String sessionId);
    void updateSession(String sessionId, Integer sourcePort, Integer destinationPort, String destinationArp);
}
