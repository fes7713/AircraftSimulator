package aircraftsimulator.GameObject.Aircraft;

import aircraftsimulator.GameObject.Aircraft.Thruster.ThrusterActionType;

public interface SwitchValueSimulator {
    Enum[] getSwitchCases();
    void simulateSwitchCase(ThrusterActionType type);
}
