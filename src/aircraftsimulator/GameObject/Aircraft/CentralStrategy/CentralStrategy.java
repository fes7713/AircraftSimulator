package aircraftsimulator.GameObject.Aircraft.CentralStrategy;

import aircraftsimulator.Game;
import aircraftsimulator.GameObject.Aircraft.Communication.Event.Event;
import aircraftsimulator.GameObject.Aircraft.Communication.Information.FireInformation;
import aircraftsimulator.GameObject.Aircraft.Communication.Information.FirePositionInformation;
import aircraftsimulator.GameObject.Aircraft.Communication.Information.Information;
import aircraftsimulator.GameObject.Aircraft.Communication.Information.PositionInformation;
import aircraftsimulator.GameObject.Aircraft.Communication.LocalRouter;
import aircraftsimulator.GameObject.Aircraft.Communication.NetwrokAdaptor.NetworkAdaptor;
import aircraftsimulator.GameObject.Aircraft.Communication.PortEnum;
import aircraftsimulator.GameObject.Aircraft.Guided;
import aircraftsimulator.GameObject.Aircraft.Spawner.CloseRangeWeaponSystem;
import aircraftsimulator.GameObject.Aircraft.Spawner.LongRangeWeaponSystem;
import aircraftsimulator.GameObject.Aircraft.Spawner.WeaponSystem;
import aircraftsimulator.GameObject.Component.Component;
import aircraftsimulator.GameObject.DestructibleObjectInterface;
import aircraftsimulator.GameObject.GameObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.vecmath.Vector3f;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class CentralStrategy extends Component implements CentralStrategyInterface {
    private GameObject parent;
    private final List<PositionInformation> detectedTargets;

    private final List<LongRangeWeaponSystem> longRangeWeaponSystems;
    private final List<CloseRangeWeaponSystem> closeRangeWeaponSystems;

    private final Map<PositionInformation, List<Guided>> guideMap;
    private final List<Guided> unguidedMissiles;

    private BehaviorPolicy policy;
    private final int maxMissilePerTarget;


    public final static int MAX_MISSILE_PER_TARGET = 1;

    public CentralStrategy(GameObject parent)
    {
        this.parent = parent;
        longRangeWeaponSystems = new LinkedList<>();
        closeRangeWeaponSystems = new LinkedList<>();

        detectedTargets = new LinkedList<>();

        guideMap = new HashMap<>();
        unguidedMissiles = new LinkedList<>();
        maxMissilePerTarget = MAX_MISSILE_PER_TARGET;
    }

    private void sortMapByDistance(List<PositionInformation> informationList, Map<Information, Float> distanceMap)
    {
        // Information to distance loop up map
        distanceMap.clear();
        distanceMap.putAll(informationList
                .stream()
                .collect(
                        Collectors.toMap(k -> k, info -> {
                            Vector3f pV = (Vector3f) info.getPosition().clone();
                            pV.sub(parent.getPosition());
                            return pV.lengthSquared();
                        })));

        // Ascending sort
        informationList.sort((v1, v2) -> (int) Math.ceil(distanceMap.get(v1) - distanceMap.get(v2)));
    }

    private PositionInformation findClosestObject(Vector3f p, @NotNull Set<PositionInformation> informationSet)
    {
        float min = Float.MAX_VALUE;
        PositionInformation selected = null;
        for(PositionInformation info : informationSet)
        {
            Vector3f a = new Vector3f(p);
            a.sub(info.getPosition());
            float len = a.lengthSquared();
            if(min > len)
            {
                min = len;
                selected = info;
            }
        }

        return selected;
    }

    private @NotNull Map<PositionInformation, PositionInformation>  newToOldPositionTracking()
    {
        Map<PositionInformation, PositionInformation> newToOldPositionMap = new HashMap<>();
        sortMapByDistance(detectedTargets, new HashMap<>());

        if(guideMap.isEmpty())
        {
            // Attack (fire missile) if null
            for(PositionInformation i: detectedTargets)
                newToOldPositionMap.put(i, null);
        }
        else{
            Set<PositionInformation> attackingSet = new HashSet<>(guideMap.keySet());
            for(int i = 0; i < detectedTargets.size(); i++)
            {
                if(attackingSet.size() == 0)
                {
                    newToOldPositionMap.put(detectedTargets.get(i), null);
                }else{
                    Vector3f p = detectedTargets.get(i).getPosition();
                    PositionInformation selectedInfo = findClosestObject(p, attackingSet);
                    attackingSet.remove(selectedInfo);
                    newToOldPositionMap.put(detectedTargets.get(i), selectedInfo);
                }
            }
        }
        return newToOldPositionMap;
    }

    private boolean activateCloseRangeWeapon(PositionInformation information, float distanceSqrt)
    {
        boolean flag = false;
        for(int i = closeRangeWeaponSystems.size() - 1; i >= 0; i--)
        {
            float range = closeRangeWeaponSystems.get(i).getRange();
            if(distanceSqrt < range * range)
            {
                FireInformation fireInformation = new FirePositionInformation(information);
                closeRangeWeaponSystems.get(i).fire(fireInformation);
                flag = true;
            }
        }
        return flag;
    }

    private void removeUntrackedPositionFromTrackingMap(Map<PositionInformation, PositionInformation> newToOldTracking)
    {
        List<PositionInformation> oldPList = new LinkedList<>(guideMap.keySet());

        for(PositionInformation oldP: oldPList)
        {
            if(!newToOldTracking.containsValue(oldP))
            {
                unguidedMissiles.addAll(guideMap.get(oldP));
                guideMap.remove(oldP);
            }
        }
    }

    // Remove if not available or dead
    private void checkGuideStatus()
    {
        Set<PositionInformation> keys = new HashSet<>(guideMap.keySet());
        for(PositionInformation info: keys)
        {
            if(info.getSource() instanceof DestructibleObjectInterface d && !d.isAlive())
            {
                guideMap.remove(info);
                continue;
            }

            // Gun>??
            // TODO ??
            if(guideMap.get(info) == null)
                continue;
            guideMap.get(info).removeIf(g -> !g.isActive());
        }
        unguidedMissiles.removeIf(g -> !g.isActive());
    }

    public void updateGuideMap(Map<PositionInformation, PositionInformation> newToOldTracking)
    {
        for(PositionInformation newP: newToOldTracking.keySet())
        {
            if(guideMap.containsKey(newToOldTracking.get(newP)))
            {
                guideMap.put(newP, guideMap.get(newToOldTracking.get(newP)));
                guideMap.remove(newToOldTracking.get(newP));
            }
        }
    }

    private void sendNewPositionToGuided()
    {
        for(PositionInformation p: guideMap.keySet())
            for(Guided g: guideMap.get(p))
                g.receive(p);
    }

    private void reassignUnguidedMissiles()
    {
        if(guideMap.isEmpty())
            return;
        int i = 0;
        Set<PositionInformation> infoList = new HashSet<>(guideMap.keySet());
        while(!unguidedMissiles.isEmpty())
        {
            Guided g = unguidedMissiles.get(0);
            PositionInformation p = findClosestObject(g.getPosition(), infoList);

            guideMap.get(p).add(g);
            unguidedMissiles.remove(g);
            System.out.println("Missile reassigned");
            i++;
        }
    }

    @Override
    public void update(float delta) {
        // Detected sorted
        Map<PositionInformation, PositionInformation> newToOldTracking = newToOldPositionTracking();
        removeUntrackedPositionFromTrackingMap(newToOldTracking);
        checkGuideStatus();
        updateGuideMap(newToOldTracking);
        reassignUnguidedMissiles();


        int j = 0;

        for(int i = 0; i < detectedTargets.size(); i++)
        {
            @NotNull
            PositionInformation newP = detectedTargets.get(i);
            List<Guided> guidedList = null;
            if(guideMap.containsKey(newP))
                guidedList = guideMap.get(newP);

            Vector3f p = new Vector3f(parent.getPosition());
            p.sub(newP.getPosition());
            float distanceSqrt = p.lengthSquared();

            // Close range
            if(activateCloseRangeWeapon(newP, distanceSqrt))
            {
                continue;
            }

            // Long range
            if(guidedList == null || guidedList.size() < maxMissilePerTarget)
            {
                if(j >=longRangeWeaponSystems.size())
                    continue;
                LongRangeWeaponSystem weaponSystem = longRangeWeaponSystems.get(j);
                while(!weaponSystem.isAvailable())
                {
                    j++;
                    if(j >= longRangeWeaponSystems.size())
                        break;
                    weaponSystem = longRangeWeaponSystems.get(j);
                }

                if(j >= longRangeWeaponSystems.size())
                    continue;

                float range = weaponSystem.getRange();
                if(range * range > distanceSqrt)
                {
                    weaponSystem.fire(newP);
                    guideMap.put(newP, new ArrayList<>());
//                    if(!guideMap.containsKey(newP))
//                    {
//                        guideMap.put(newP, new ArrayList<>(unguidedMissiles));
//                        unguidedMissiles.clear();
//                    }
                    j++;
                }
            }
        }

        sendNewPositionToGuided();
        detectedTargets.clear();
    }

    @Override
    public void draw(Graphics2D g2d) {

    }

    @Override
    public void setParent(GameObject parent) {
        this.parent = parent;
    }

    public void addWeaponSystem(WeaponSystem weaponSystem)
    {
        switch(weaponSystem)
        {
            case LongRangeWeaponSystem lrws -> {
                longRangeWeaponSystems.add(lrws);
                longRangeWeaponSystems.sort((w1, w2) -> (int)Math.ceil(w1.getRange() - w2.getRange()));
            }
            case CloseRangeWeaponSystem crws -> {
                closeRangeWeaponSystems.add(crws);
                closeRangeWeaponSystems.sort((w1, w2) -> (int)Math.ceil(w1.getRange() - w2.getRange()));
            }
            default -> throw new RuntimeException("Error in weapon system in center strategy");
        }
    }

    public void removeWeaponSystem(WeaponSystem weaponSystem)
    {
        switch(weaponSystem)
        {
            case LongRangeWeaponSystem lrws -> {
                longRangeWeaponSystems.remove(lrws);
                longRangeWeaponSystems.sort((w1, w2) -> (int)Math.ceil(w1.getRange() - w2.getRange()));
            }
            case CloseRangeWeaponSystem crws -> {
                closeRangeWeaponSystems.remove(crws);
                closeRangeWeaponSystems.sort((w1, w2) -> (int)Math.ceil(w1.getRange() - w2.getRange()));
            }
            default -> throw new RuntimeException("Error in weapon system in center strategy");
        }
    }

    public void addToGuidance(Guided guided, PositionInformation information){
        List<Guided> list = guideMap.getOrDefault(information, null);
        System.out.println(Game.getFrames() + "Launch");
        if(list == null)
        {
            unguidedMissiles.add(guided);
        }
        else
            list.add(guided);
    }

    public void removeFromGuidance(Guided guided){
        PositionInformation key = null;
        for(PositionInformation p: guideMap.keySet())
        {
            if(guideMap.get(p).contains(guided))
            {
                key = p;
                break;
            }
        }
        guideMap.remove(key);
    }

    public void receive(@Nullable Information information) {
        if(information instanceof PositionInformation p)
        {
            // TODO may need to chgange when we accept the multiple info for the same target.
            for(PositionInformation pi: detectedTargets)
            {
                if(pi.getSource() == p.getSource())
                    return;
            }
            detectedTargets.add(p);
        }

    }
}
