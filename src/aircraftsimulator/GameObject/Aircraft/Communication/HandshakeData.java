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
}
