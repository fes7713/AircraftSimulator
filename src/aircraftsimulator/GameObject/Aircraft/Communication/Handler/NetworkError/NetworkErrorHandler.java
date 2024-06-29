package aircraftsimulator.GameObject.Aircraft.Communication.Handler.NetworkError;

public interface NetworkErrorHandler {
    void handle(int port, NetworkErrorType type);
}
