package aircraftsimulator.GameObject.Aircraft.Radar;

import aircraftsimulator.GameObject.Aircraft.Communication.Data.Data;

import javax.vecmath.Vector3f;
import java.util.List;

public record RadarData(List<Vector3f> positionList) implements Data {
}
