package aircraftsimulator.GameObject.Aircraft.Communication.Handler.NetworkError;

public class DefaultNetworkErrorHandler implements NetworkErrorHandler{
    @Override
    public void handle(int port, NetworkErrorType type) {
        System.out.printf("Port [%3d] Error [%s]\n", port, type.name());
    }
}
