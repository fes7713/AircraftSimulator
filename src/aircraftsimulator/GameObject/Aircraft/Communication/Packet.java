package aircraftsimulator.GameObject.Aircraft.Communication;

import org.jetbrains.annotations.NotNull;

public class Packet<T> {
    protected final T data;
    protected final Integer sourcePort;
    protected final Integer destinationPort;

    protected Long created;
    protected String sessionID;

    public Packet(Packet packet, T data, Integer sourcePort) {
        this(packet.getSessionID(), data, sourcePort, null);
    }

    public Packet(Packet packet, T data, Integer sourcePort, Integer destinationPort) {
        this(packet.getSessionID(), data, sourcePort, destinationPort);
    }

    public Packet(String sessionID, T data, Integer sourcePort) {
        this(sessionID, data, sourcePort, null);
    }

    public Packet(@NotNull String sessionID, T data, Integer sourcePort, Integer destinationPort) {
        if(sourcePort == null)
            throw new IllegalArgumentException("sourcePort cannot be null");
        this.sessionID = sessionID;
        this.data = data;
        this.sourcePort = sourcePort;
        this.destinationPort = destinationPort;

        created = System.currentTimeMillis();
    }

    public T getData()
    {
        return data;
    }

    @NotNull
    public Integer getSourcePort()
    {
        return sourcePort;
    }

    public Integer getDestinationPort(){
        return destinationPort;
    }

    public String getSessionID() {
        return sessionID;
    }

    public Long getCreated()
    {
        return created;
    }
}
