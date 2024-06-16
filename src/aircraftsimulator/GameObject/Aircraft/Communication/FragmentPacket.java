package aircraftsimulator.GameObject.Aircraft.Communication;

import org.jetbrains.annotations.NotNull;

public class FragmentPacket extends Packet{

    public FragmentPacket(String sessionID, SessionInformation info, HandshakeData handshakeData, byte[] data, String sourceMac) {
        super(sessionID, info, handshakeData, data, sourceMac);
    }

    public FragmentPacket(HandshakeData handshakeData, byte[] data, @NotNull Integer sourcePort, @NotNull Integer destinationPort, String sourceMac, String destinationMac) {
        super(handshakeData, data, sourcePort, destinationPort, sourceMac, destinationMac);
    }
}
