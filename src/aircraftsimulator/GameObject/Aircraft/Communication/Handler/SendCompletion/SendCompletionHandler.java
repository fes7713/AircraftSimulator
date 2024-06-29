package aircraftsimulator.GameObject.Aircraft.Communication.Handler.SendCompletion;

import aircraftsimulator.GameObject.Aircraft.Communication.Handler.Handler;

public interface SendCompletionHandler extends Handler {
    void handle(int port);
}
