package aircraftsimulator.GameObject.Aircraft.CentralStrategy;

import aircraftsimulator.GameObject.Aircraft.Bullet;
import aircraftsimulator.GameObject.Aircraft.Communication.Information.Information;
import aircraftsimulator.GameObject.Aircraft.DamageGenerator;

import javax.vecmath.Vector3f;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

public class CentralStrategy implements CentralStrategyInterface{
    // Distance information
    List<Information> detectedTargets;
//
//    // Range??
    List<DamageGenerator> damageGenerators;
//
//    Map<Component, List<Information>> detectionMap;
//
//    Map<DamageGenerator, List<Information>> attackingMap;

    private BehaviorPolicy policy;

    public CentralStrategy()
    {
        damageGenerators = new LinkedList<>();
    }


    // Ascending
    private void sortDamageGeneratorsByRange()
    {
        damageGenerators.sort((v1, v2) -> (int) Math.ceil(v1.getRange() - v2.getRange()));
    }

    public void addDamageGenerator(DamageGenerator damageGenerator)
    {
        damageGenerators.add(damageGenerator);
        sortDamageGeneratorsByRange();
    }

    public static void main(String[] args)
    {
        CentralStrategy centralStrategy = new CentralStrategy();
        Bullet bullet = new Bullet(null, null, null, new Vector3f(50, 0, 0), 50);
        Bullet bullet1 = new Bullet(null, null, null, new Vector3f(60, 0, 0), 50);
        Bullet bullet2 = new Bullet(null, null, null, new Vector3f(80, 0, 0), 50);
        Bullet bullet3 = new Bullet(null, null, null, new Vector3f(30, 0, 0), 50);

        Stream.of(bullet, bullet1, bullet2, bullet3).forEach(centralStrategy::addDamageGenerator);
    }
}
