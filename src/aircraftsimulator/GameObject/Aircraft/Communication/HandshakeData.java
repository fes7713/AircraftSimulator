package aircraftsimulator.GameObject.Aircraft.Communication;

public record HandshakeData(boolean syn, boolean ack, boolean rst, boolean fin) {

    public boolean isSyn() {
        return syn;
    }

    public boolean isAck() {
        return ack;
    }

    public boolean isRst()
    {
        return rst;
    }

    public boolean isFin() {
        return fin;
    }

    public static final HandshakeData EMPTY = new HandshakeData(false, false, false, false);
    public static final HandshakeData SYN = new HandshakeData(true, false, false, false);
    public static final HandshakeData SYN_ACK = new HandshakeData(true, true, false, false);
    public static final HandshakeData ACK = new HandshakeData(false, true, false, false);
    public static final HandshakeData FIN = new HandshakeData(false, false, false, true);
    public static final HandshakeData FIN_ACK = new HandshakeData(false, true, false, true);
    public static final HandshakeData RST = new HandshakeData(false, false, true, false);
    public static final HandshakeData RST_ACK = new HandshakeData(false, true, true, false);
}
