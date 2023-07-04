package aircraftsimulator.GameObject.Aircraft.Communication;

import aircraftsimulator.GameObject.Aircraft.Communication.Information.Information;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class InformationNetwork implements ReceiverInterface {
    private final List<ReceiverInterface> receivers;

    public InformationNetwork()
    {
        receivers = new ArrayList<>();
    }

    public void addReceiver(ReceiverInterface receiverInterface)
    {
        receivers.add(receiverInterface);
    }

    public void removeReceiver(ReceiverInterface receiverInterface)
    {
        receivers.remove(receiverInterface);
    }

    @Override
    public void receive(@Nullable Information information) {
        for(ReceiverInterface r: receivers)
            r.receive(information);
    }
}
