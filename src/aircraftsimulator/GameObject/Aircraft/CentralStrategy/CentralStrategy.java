package aircraftsimulator.GameObject.Aircraft.CentralStrategy;

import aircraftsimulator.GameObject.Aircraft.Communication.Information.FireInformation;
import aircraftsimulator.GameObject.Aircraft.Communication.Information.FirePositionInformation;
import aircraftsimulator.GameObject.Aircraft.Communication.Information.Information;
import aircraftsimulator.GameObject.Aircraft.Communication.Information.PositionInformation;
import aircraftsimulator.GameObject.Aircraft.Communication.ReceiverInterface;
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

public class CentralStrategy extends Component implements CentralStrategyInterface, ReceiverInterface {
    // Distance information
    private GameObject parent;
    private final List<PositionInformation> detectedTargets;
//
//    // Range??
    // Missiles
    private final List<LongRangeWeaponSystem> longRangeWeaponSystems;

    // Guns. Autostart
    private final List<CloseRangeWeaponSystem> closeRangeWeaponSystems;
//
//    Map<Component, List<Information>> detectionMap;
//
//    Map<DamageGenerator, List<Information>> attackingMap;

    private final Map<PositionInformation, List<Guided>> guideMap;

    private BehaviorPolicy policy;

    public CentralStrategy(GameObject parent)
    {
        this.parent = parent;
        longRangeWeaponSystems = new LinkedList<>();
        closeRangeWeaponSystems = new LinkedList<>();

        detectedTargets = new LinkedList<>();

        guideMap = new HashMap<>();
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

    private PositionInformation findClosestObject(Vector3f p, Set<PositionInformation> informationSet)
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

    public Map<PositionInformation, PositionInformation>  newToOldPositionTracking()
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
            if(distanceSqrt < closeRangeWeaponSystems.get(i).getRange())
            {
                FireInformation fireInformation = new FirePositionInformation(information);
                closeRangeWeaponSystems.get(i).fire(fireInformation);
                guideMap.put(fireInformation, null);
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

            guideMap.get(info).removeIf(g -> !g.isActive());
        }
    }

    @Override
    public void update(float delta) {
        // Detected sorted
        Map<PositionInformation, PositionInformation> newToOldTracking = newToOldPositionTracking();
        removeUntrackedPositionFromTrackingMap(newToOldTracking);
        checkGuideStatus();

        int j = 0;

        for(int i = 0; i < detectedTargets.size(); i++)
        {
            @NotNull
            PositionInformation newP = detectedTargets.get(i);
            @Nullable
            PositionInformation oldP = newToOldTracking.get(newP);
            List<Guided> guidedList = null;
            if(oldP != null)
                guidedList = guideMap.get(oldP);

            Vector3f p = new Vector3f(parent.getPosition());
            p.sub(newP.getPosition());
            float distanceSqrt = p.lengthSquared();

            // Close range
            if(activateCloseRangeWeapon(newP, distanceSqrt))
            {
                guideMap.remove(oldP);
                continue;
            }

            // Long range
            if(guidedList == null || guidedList.isEmpty())
            {
                if(longRangeWeaponSystems.size() == 0)
                    return;
                LongRangeWeaponSystem weaponSystem = longRangeWeaponSystems.get(j);
                while(!weaponSystem.isAvailable())
                {
                    j++;
                    if(j >= longRangeWeaponSystems.size())
                        break;
                    weaponSystem = longRangeWeaponSystems.get(j);
                }

                float range = weaponSystem.getRange();
                if(range * range > distanceSqrt)
                {
                    FireInformation fireInformation = new FirePositionInformation(newP);
                    weaponSystem.fire(fireInformation);
                    guideMap.put(fireInformation, new ArrayList<>());
                    guideMap.remove(oldP);
                    j++;
                }
            }
            else{
                guideMap.put(newP, guidedList);
                guideMap.remove(oldP);
                for(Guided g: guidedList)
                {
                    g.receive(newP);
                }
            }
        }
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

    public void addToGuidance(Guided guided, PositionInformation information){
        List<Guided> list = guideMap.getOrDefault(information, null);
        if(list == null)
        {
            guideMap.put(information, new ArrayList<>(List.of(guided)));
            System.out.println("Gun attack??");
        }
        else
            list.add(guided);
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

    @Override
    public void receive(@Nullable Information information) {
        if(information instanceof PositionInformation p)
            detectedTargets.add(p);
    }
}
