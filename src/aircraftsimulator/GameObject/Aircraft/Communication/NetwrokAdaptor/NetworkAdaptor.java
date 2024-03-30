package aircraftsimulator.GameObject.Aircraft.Communication.NetwrokAdaptor;

import aircraftsimulator.GameObject.Aircraft.Communication.Router;

public interface NetworkAdaptor {
    NetworkInterface getNetworkInterface();

    default void disconnect() {
        Router router = getNetworkInterface().getRouter();
        if(router != null)
            router.removeRouting(this);
    }
}
