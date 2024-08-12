package aircraftsimulator.GameObject.Aircraft.Radar.Radar;

import aircraftsimulator.GameObject.Aircraft.Communication.Data.Data;

public record SearchingRequest(String uuid) implements Data {
}
