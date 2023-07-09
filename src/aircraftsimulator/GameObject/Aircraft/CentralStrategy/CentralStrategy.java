package aircraftsimulator.GameObject.Aircraft.CentralStrategy;

import aircraftsimulator.GameObject.Aircraft.Communication.Information.Information;
import aircraftsimulator.GameObject.Aircraft.Communication.Information.PositionInformation;
import aircraftsimulator.GameObject.Aircraft.DamageGenerator;
import aircraftsimulator.GameObject.Component.Component;
import aircraftsimulator.GameObject.GameObject;

import javax.vecmath.Vector3f;
import java.awt.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CentralStrategy extends Component implements CentralStrategyInterface{
    // Distance information
    private GameObject parent;
    private final List<PositionInformation> detectedTargets;
    private Map<Information, Float> targetDistanceMap;
//
//    // Range??
    private final List<DamageGenerator> damageGenerators;
//
//    Map<Component, List<Information>> detectionMap;
//
//    Map<DamageGenerator, List<Information>> attackingMap;

    private BehaviorPolicy policy;

    public CentralStrategy(GameObject parent)
    {
        this.parent = parent;
        damageGenerators = new LinkedList<>();
        detectedTargets = new LinkedList<>();
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

    @Override
    public void update(float delta) {
        // Send info to components
        targetDistanceMap = detectedTargets
                .stream()
                .collect(
                    Collectors.toMap(k -> k, info -> {
                        Vector3f pV = (Vector3f) info.getPosition().clone();
                        pV.sub(parent.getPosition());
                        return pV.lengthSquared();
                    }));

        // Ascending sort
        detectedTargets.sort((v1, v2) -> (int) Math.ceil(targetDistanceMap.get(v1) - targetDistanceMap.get(v2)));

        int j = 0;
        if(damageGenerators.size() <= 0)
            return;
        for(int i = 0; i < detectedTargets.size(); i++)
        {
            PositionInformation info = detectedTargets.get(i);
            if(targetDistanceMap.get(info) < damageGenerators.get(j).getRange())
            {
                // TODO
//                damageGenerators.get(j).r
            }
        }
    }

    @Override
    public void draw(Graphics2D g2d) {

    }

    @Override
    public void setParent(GameObject parent) {
        this.parent = parent;
    }

    public static void main(String[] args)
    {
//        // TODO
//        CentralStrategy centralStrategy = new CentralStrategy();
//        Bullet bullet = new Bullet(null, null, null, new Vector3f(50, 0, 0), 50);
//        Bullet bullet1 = new Bullet(null, null, null, new Vector3f(60, 0, 0), 50);
//        Bullet bullet2 = new Bullet(null, null, null, new Vector3f(80, 0, 0), 50);
//        Bullet bullet3 = new Bullet(null, null, null, new Vector3f(30, 0, 0), 50);
//
//        Stream.of(bullet, bullet1, bullet2, bullet3).forEach(centralStrategy::addDamageGenerator);
    }
}
