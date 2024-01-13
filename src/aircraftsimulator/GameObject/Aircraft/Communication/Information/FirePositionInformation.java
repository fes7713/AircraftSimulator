package aircraftsimulator.GameObject.Aircraft.Communication.Information;

import aircraftsimulator.GameObject.GameObjectInterface;
import org.jetbrains.annotations.NotNull;

import javax.vecmath.Vector3f;

public class FirePositionInformation  extends PositionInformationImp implements FireInformation{
    public FirePositionInformation(GameObjectInterface source, Vector3f position) {
        super(source, position);
    }

    public FirePositionInformation(@NotNull PositionInformation info) {
        super(info.getSource(), info.getPosition());
    }
}
