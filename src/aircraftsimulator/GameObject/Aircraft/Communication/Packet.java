package aircraftsimulator.GameObject.Aircraft.Communication;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class Packet {
    protected final byte[] data;
    protected final HandshakeData handshakeData;
    protected final Integer sourcePort;
    protected final Integer destinationPort;

    protected final String sourceMac;
    protected final String destinationMac;

    protected Long created;
    protected String sessionID;

    public Packet(String sessionID, SessionInformation info, HandshakeData handshakeData, byte[] data, String sourceMac)
    {
        this(handshakeData, data, info.sourcePort(), info.destinationPort(), sourceMac, info.destinationMac());
        this.sessionID = sessionID;
    }

    public Packet(HandshakeData handshakeData, byte[] data, @NotNull Integer sourcePort, @NotNull Integer destinationPort, String sourceMac, String  destinationMac)
    {
        this.handshakeData = handshakeData;
        this.data = data == null ? "".getBytes() : data;
        this.sourcePort = sourcePort;
        this.destinationPort = destinationPort;
        this.sourceMac = sourceMac;
        this.destinationMac = destinationMac;

        created = System.currentTimeMillis();
    }

    private Packet(String sessionID, HandshakeData handshakeData, byte[] data, @NotNull Integer sourcePort, @NotNull Integer destinationPort, String sourceMac, String  destinationMac) {
        this(handshakeData, data, sourcePort, destinationPort, sourceMac, destinationMac);
        this.sessionID = sessionID;
    }

    public HandshakeData getHandshake()
    {
        return handshakeData;
    }

    public byte[] getData()
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

    public SessionInformation getSessionInformation()
    {
        return new SessionInformation(sourcePort, destinationPort, destinationMac);
    }

    public Packet copy(String sessionID)
    {
        return new Packet(sessionID, handshakeData, Arrays.copyOf(data, data.length), sourcePort, destinationPort, sourceMac, destinationMac);
    }

    public Packet copy(byte[] data)
    {
        return new Packet(sessionID, handshakeData, data, sourcePort, destinationPort, sourceMac, destinationMac);
    }
}
