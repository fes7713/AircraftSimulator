package aircraftsimulator.GameObject.Aircraft.Communication;

import java.io.Serializable;

public record PositionCommand (int x, String title) implements Serializable {

}
