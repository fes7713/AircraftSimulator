package aircraftsimulator.GameObject.Aircraft.Communication.Handler.SendCompletion;

import aircraftsimulator.GameObject.Aircraft.Communication.Logger.Logger;

public class DefaultSendCompletionHandler implements SendCompletionHandler {
    @Override
    public void handle(int port) {
        Logger.Log(Logger.LogLevel.INFO, "Data Send Completed", "", port);
    }
}
