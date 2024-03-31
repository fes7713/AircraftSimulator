package aircraftsimulator.GameObject.Aircraft.Communication;

public record HandshakeData(boolean syn, boolean ack, boolean fin, Integer requestingPort, String mac) {

    public boolean isSyn() {
        return syn;
    }

    public boolean isAck() {
        return ack;
    }

    public boolean isFin() {
        return fin;
    }

    public Integer getRequestingPort() {
        return requestingPort;
    }

    public String getMac()
    {
        return mac;
    }
}
