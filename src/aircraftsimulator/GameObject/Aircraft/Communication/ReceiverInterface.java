package aircraftsimulator.GameObject.Aircraft.Communication;

import aircraftsimulator.GameObject.Aircraft.Communication.Information.Information;
import org.jetbrains.annotations.Nullable;

public interface ReceiverInterface {
    void receive(@Nullable Information information);
}
