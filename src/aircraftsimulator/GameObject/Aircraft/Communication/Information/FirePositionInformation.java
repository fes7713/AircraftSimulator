package aircraftsimulator.GameObject.Aircraft.Communication.Information;

import aircraftsimulator.GameObject.GameObject;
import org.jetbrains.annotations.NotNull;

import javax.vecmath.Vector3f;

public class FirePositionInformation  extends PositionInformationImp implements FireInformation{
    public FirePositionInformation(GameObject source, Vector3f position) {
        super(source, position);
    }

    public FirePositionInformation(@NotNull PositionInformation info) {
        super(info.getSource(), info.getPosition());
    }
}
