package aircraftsimulator.GameObject;

import aircraftsimulator.GameObject.Aircraft.Communication.Information.LaserInformation;

import javax.vecmath.Vector3f;

public interface ReflectionInterface {
    LaserInformation reflect(Vector3f direction, float frequency, float intensity);
}
