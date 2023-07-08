package aircraftsimulator.GameObject.Aircraft.CentralStrategy;

import aircraftsimulator.GameObject.Aircraft.Communication.Information.Information;
import aircraftsimulator.GameObject.Aircraft.DamageGenerator;

import java.util.List;

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

    private void sortDamageGeneratorsByRange()
    {

    }
}
