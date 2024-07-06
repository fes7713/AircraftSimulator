package aircraftsimulator.GameObject.Aircraft;

import aircraftsimulator.GameObject.Aircraft.Communication.Information.Information;
import aircraftsimulator.GameObject.Team;

import javax.vecmath.Vector3f;
import java.awt.*;

public class GuidedMissile extends Missile implements Guided, Cloneable {
//    public GuidedMissile(Missile m) {
//        super(m);
//
//    }

    public GuidedMissile(Team team, float health, float baseDamage) {
        super(team, health, baseDamage);
    }

    public GuidedMissile(Team team, Information target, Vector3f position, Vector3f velocity, float health, float baseDamage) {
        super(team, target, position, velocity, health, baseDamage);
    }

    public GuidedMissile(Team team, Information target, Vector3f position, Vector3f velocity, Color color, float size, float health, float thrusterMagnitude, float baseDamage) {
        super(team, target, position, velocity, color, size, health, thrusterMagnitude, baseDamage);
    }

    @Override
    public boolean isActive() {
        return isAlive();
    }


    // TODO do something toreplace it with super.clone()
    @Override
    public GuidedMissile clone() {
        return null;
    }
}
