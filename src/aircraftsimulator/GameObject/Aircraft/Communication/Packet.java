package aircraftsimulator.GameObject.Aircraft.Communication;

import org.jetbrains.annotations.NotNull;

public class Packet<T> {
    protected final T data;
    protected final Integer sourcePort;
    protected final Integer destinationPort;

    protected final String sourceMac;
    protected final String destinationMac;

    protected Long created;
    protected String sessionID;

    public Packet(Packet<T> packet, String sessionID)
    {
        this(sessionID, packet.data, packet.sourcePort, packet.destinationPort, packet.sourceMac, packet.destinationMac);
    }

    public Packet(@NotNull Packet receivedPacket, T data, @NotNull String sourceMac)
    {
        this(receivedPacket.sessionID, data, receivedPacket.destinationPort, receivedPacket.sourcePort, sourceMac, receivedPacket.sourceMac);
    }

    public Packet(@NotNull Packet receivedPacket, T data, @NotNull Integer sourcePort, @NotNull Integer destinationPort, String sourceMac, String  destinationMac)
    {
        this(receivedPacket.sessionID, data, sourcePort, destinationPort, sourceMac, destinationMac);
    }

    public Packet(T data, @NotNull Integer sourcePort, @NotNull Integer destinationPort, String sourceMac, String  destinationMac)
    {
        this.data = data;
        this.sourcePort = sourcePort;
        this.destinationPort = destinationPort;
        this.sourceMac = sourceMac;
        this.destinationMac = destinationMac;

        created = System.currentTimeMillis();
    }

    public Packet(String sessionID, T data, @NotNull Integer sourcePort, @NotNull Integer destinationPort, String sourceMac, String  destinationMac) {
        this(data, sourcePort, destinationPort, sourceMac, destinationMac);
        this.sessionID = sessionID;
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

    @NotNull
    public Integer getDestinationPort(){
        return destinationPort;
    }

    public String getSessionID() {
        return sessionID;
    }

    public void setSessionID(String sessionID)
    {
        this.sessionID = sessionID;
    }

    public String getSourceMac() {
        return sourceMac;
    }

    public String getDestinationMac() {
        return destinationMac;
    }

    public Long getCreated()
    {
        return created;
    }
}
