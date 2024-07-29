package aircraftsimulator.GameObject.Aircraft.FlightController;

import aircraftsimulator.GameObject.Aircraft.Communication.Data.Data;

import javax.vecmath.Vector3f;

public record PositionData(Vector3f position) implements Data {
}
