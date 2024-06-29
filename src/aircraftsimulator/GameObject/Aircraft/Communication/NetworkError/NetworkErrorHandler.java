package aircraftsimulator.GameObject.Aircraft.Communication.NetworkError;

public interface NetworkErrorHandler {
    void handle(int port, NetworkErrorType type);
}
