package aircraftsimulator.GameObject.Aircraft.Radar.Radar;

import aircraftsimulator.GameObject.Aircraft.Communication.Data.Data;

import javax.vecmath.Vector3f;

public record TrackingRequest(String uuid, Vector3f position) implements Data {

}
