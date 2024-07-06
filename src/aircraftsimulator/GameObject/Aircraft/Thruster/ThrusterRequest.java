package aircraftsimulator.GameObject.Aircraft.Thruster;

import aircraftsimulator.GameObject.Aircraft.Communication.Data.Data;

public record ThrusterRequest(ThrusterLevel level) implements Data {
}
