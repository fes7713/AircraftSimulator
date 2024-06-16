package aircraftsimulator.GameObject.Aircraft.Communication;

public record SessionInformation(Integer sourcePort, Integer destinationPort, String destinationMac) {
    public SessionInformation reverse(String myMac) {
        return new SessionInformation(destinationPort, sourcePort, myMac);
    }
}