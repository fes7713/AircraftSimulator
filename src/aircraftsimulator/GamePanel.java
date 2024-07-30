package aircraftsimulator;

import aircraftsimulator.Animation.AnimationManager;
import aircraftsimulator.GameObject.Aircraft.Aircraft;
import aircraftsimulator.GameObject.Aircraft.CentralStrategy.SimpleStrategy;
import aircraftsimulator.GameObject.Aircraft.Communication.Data.ConnectRequest;
import aircraftsimulator.GameObject.Aircraft.Communication.Information.LaserInformation;
import aircraftsimulator.GameObject.Aircraft.FlightController.PositionData;
import aircraftsimulator.GameObject.Aircraft.FlightController.v2.SimpleFlightController;
import aircraftsimulator.GameObject.Aircraft.Radar.Radar.AngleRadar;
import aircraftsimulator.GameObject.Aircraft.Radar.Radar.RadioCommunicator;
import aircraftsimulator.GameObject.Aircraft.Radar.RadarFrequency;
import aircraftsimulator.GameObject.Aircraft.Radar.Wave.ElectroMagneticWave;
import aircraftsimulator.GameObject.Aircraft.SystemPort;
import aircraftsimulator.GameObject.Aircraft.Thruster.SimpleThruster;
import aircraftsimulator.GameObject.GameObject;
import aircraftsimulator.GameObject.Team;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.vecmath.Vector3f;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.List;
import java.util.*;
import java.util.stream.Stream;

public class GamePanel extends JPanel {
    private final aircraftsimulator.Environment environment;
    private final List<GameObject> objects;
    private final Map<Float, List<LaserInformation>> laserMap;
    private final Map<LaserInformation, Float>laserDrawTimeMap;
    private final Map<Team, List<GameObject>> teamDetectionObjectMap;
    private final AnimationManager animationManager;
    private final Set<ElectroMagneticWave> emWaves;
    private final Map<GameObject, List<ElectroMagneticWave>> sensorStackMap;
    private final Map<GameObject, List<ElectroMagneticWave>> detectedWaveMap;

    public final static float LASER_DRAW_TIME = 1.5F;

    public GamePanel(Environment environment){
        this.environment = environment;
        animationManager = AnimationManager.getInstance();
        objects = new ArrayList<>();
        teamDetectionObjectMap = new HashMap<>();
        laserMap = new HashMap<>();
        laserDrawTimeMap = new HashMap<>();
        emWaves = new HashSet<>();
        sensorStackMap = new HashMap<>();
        detectedWaveMap = new HashMap<>();

//        objects.add(new GameObject(new Vector3f(100, 100, 100), Color.CYAN, 5));
        Team A = newTeam();
        Team B = newTeam();
        Team C = newTeam();
        Aircraft aircraftAcc = new Aircraft(A,
                new Vector3f(100, 100, 100),
                new Vector3f(1, 0, 0), Color.ORANGE, 5, 100);

//        aircraftAcc.setThruster(new VariableThruster(aircraftAcc, Aircraft.THRUSTER_MAGNITUDE * 2, 3600));
//        aircraftAcc.addComponent(new SimpleRadar(aircraftAcc, 100, info -> {
//            aircraftAcc.receive(info);
//        }));
        aircraftAcc.setThruster(new SimpleThruster(aircraftAcc, Aircraft.THRUSTER_MAGNITUDE, Aircraft.THRUSTER_FUEL));
        aircraftAcc.addComponent(new SimpleFlightController(aircraftAcc, aircraftAcc.getNetwork()), SystemPort.FLIGHT_CONTROL, new PositionData(new Vector3f(1000, 1000, 100)));
        aircraftAcc.addComponent(new AngleRadar(aircraftAcc, aircraftAcc.getNetwork(), RadarFrequency.C, 400, 500000, 0.2F, 1), SystemPort.SEARCH_RADAR, new ConnectRequest());
        aircraftAcc.addComponent(new RadioCommunicator(aircraftAcc, aircraftAcc.getNetwork(), RadarFrequency.L, 500000, 1F, 1), SystemPort.COMMUNICATION, new ConnectRequest());
        aircraftAcc.addComponent(new SimpleStrategy(aircraftAcc, aircraftAcc.getNetwork()), SystemPort.STRATEGY, new ConnectRequest());

//        aircraftAcc.addComponent(new Gun(aircraftAcc, 0.2F, 2, 50));

//        Missile missile = new GuidedMissile(A, 100, 80);
//        aircraftAcc.addComponent(new MissileLauncher(aircraftAcc, missile, 1F, 6));

        Aircraft aircraftAcc1 = new Aircraft(A,
                new Vector3f(150, 130, 100),
                new Vector3f(2, 0, 0), Color.BLUE, 5, 100);

        aircraftAcc1.setThruster(new SimpleThruster(aircraftAcc1, Aircraft.THRUSTER_MAGNITUDE * 2, Aircraft.THRUSTER_FUEL));
        aircraftAcc1.addComponent(new SimpleFlightController(aircraftAcc1, aircraftAcc1.getNetwork()), SystemPort.FLIGHT_CONTROL, new PositionData(new Vector3f(1000, 1000, 100)));
        aircraftAcc1.addComponent(new AngleRadar(aircraftAcc1, aircraftAcc1.getNetwork(), RadarFrequency.C, 2000, 500000, 0.2F, 1), SystemPort.SEARCH_RADAR, new ConnectRequest());
        aircraftAcc1.addComponent(new RadioCommunicator(aircraftAcc1, aircraftAcc1.getNetwork(), RadarFrequency.L, 500000, 1F, 1), SystemPort.COMMUNICATION, new ConnectRequest());
        aircraftAcc1.addComponent(new SimpleStrategy(aircraftAcc1, aircraftAcc1.getNetwork()), SystemPort.STRATEGY, new ConnectRequest());

//        aircraftAcc1.setThruster(new VariableThruster(aircraftAcc1, Aircraft.THRUSTER_MAGNITUDE * 2, 3600));
//        aircraftAcc.addComponent(new SimpleRadar(aircraftAcc, 100, info -> {
//            aircraftAcc.receive(info);
//        }));

//        aircraftAcc1.addComponent(new AngleRadar(aircraftAcc1, RadarFrequency.X,1000, 60, aircraftAcc1.getDirection()));
//        aircraftAcc1.addComponent(new Gun(aircraftAcc1, 0.2F, 2, 50));
//
//        Missile missile1 = new GuidedMissile(B, 10, 80);
//        aircraftAcc1.addComponent(new MissileLauncher(aircraftAcc1, missile1, 4, 2));

//        Aircraft aircraftAcc2 = aircraftAcc1.clone();
//        aircraftAcc2.activate(
//                new Vector3f(1000, 90, 100),
//                new Vector3f(-1, 0, 0),
//                new Vector3f(-1, 0, 0)
//        );

//        Missile missile1 = new Missile(A, aircraft1.send(MotionInformation.class), new Vector3f(90, 95, 100),
//                new Vector3f(16, 0, 0), 100, 129);
//        aircraftAcc.addToNetwork(missile1);

//        DestructibleStationaryObject target = new DestructibleStationaryObject(C, new Vector3f(100, 500, 100), Color.GREEN, 5, 100);
        GameObject object = new GameObject(B, new Vector3f(600, 50, 50), Color.BLACK, 10);
        Stream.of(aircraftAcc, aircraftAcc1, object).forEach(this::addObject);

//        emWaves.add(new ElectroMagneticWave(new Vector3f(100, 100, 100), 5000, RadarFrequency.Ku, new Vector3f(1, 0, 0), 360, "AA"));
    }

    public void update(float delta)
    {
        laserMap.clear();
        detectedWaveMap.clear();
        detectedWaveMap.putAll(sensorStackMap);
        sensorStackMap.clear();
        for(LaserInformation laser: new HashSet<>(laserDrawTimeMap.keySet()))
        {
            if(laserDrawTimeMap.get(laser) - delta > 0)
                laserDrawTimeMap.put(laser, laserDrawTimeMap.get(laser) - delta);
            else
                laserDrawTimeMap.remove(laser);
        }
        animationManager.update(delta);
        for(int i = 0; i < objects.size(); i++)
            objects.get(i).componentUpdate(delta);

        for(int i = 0; i < objects.size(); i++)
            objects.get(i).update(delta);

        for(ElectroMagneticWave wave: new HashSet<>(emWaves))
        {
            if(wave.getIntensity() < Environment.ENVIRONMENTAL_WAVE)
                emWaves.remove(wave);
            else
                wave.update(delta);
        }
    }

    public List<ElectroMagneticWave> detectEMWave(GameObject object)
    {
        return detectedWaveMap.getOrDefault(object, new ArrayList<>());
    }

    public Team newTeam()
    {
        Team team = new Team("" + (teamDetectionObjectMap.size() + 1));
        teamDetectionObjectMap.put(team, new ArrayList<>());
        return team;
    }

    @NotNull
    public Set<GameObject> getObjects()
    {
        return new HashSet<>()
        {
            {
                for(List<GameObject> objects: teamDetectionObjectMap.values())
                    addAll(objects);
            }
        };
    }

    @NotNull
    public List<GameObject> getObjects(Team team)
    {
        return teamDetectionObjectMap.get(team);
    }

    public void addObject(GameObject o)
    {
        objects.add(o);
        for(Map.Entry<Team, List<GameObject>> e: teamDetectionObjectMap.entrySet())
        {
            if(e.getKey() != o.getTeam())
            e.getValue().add(o);
        }
        for(ElectroMagneticWave wave: emWaves)
            wave.addObject(o);
    }

    public void removeObject(GameObject o)
    {
        objects.remove(o);
        for(Map.Entry<Team, List<GameObject>> e: teamDetectionObjectMap.entrySet())
            e.getValue().remove(o);
    }

    public void addLaser(@NotNull LaserInformation laserInformation)
    {
        if(!laserMap.containsKey(laserInformation.getFrequency()))
            laserMap.put(laserInformation.getFrequency(), new ArrayList<>());
//        boolean flag = laserMap.get(laserInformation.getFrequency())
//                .stream()
//                .anyMatch(laser -> laser.getInformation().getSource() == laserInformation.getSource());
//        if(!flag)
        laserMap.get(laserInformation.getFrequency()).add(laserInformation);
        laserDrawTimeMap.put(laserInformation, LASER_DRAW_TIME);
    }

    public void addPulseWave(ElectroMagneticWave wave) {
        emWaves.add(wave);
    }

    public List<LaserInformation> getLasers(float frequency)
    {
        return laserMap.get(frequency);
    }

    public void addWaveToSensor(GameObject object, ElectroMagneticWave wave)
    {
        if(!sensorStackMap.containsKey(object))
            sensorStackMap.put(object, new ArrayList<>());
        sensorStackMap.get(object).add(wave);
    }

    public Set<ElectroMagneticWave> getEMWaves()
    {
        return emWaves;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        AffineTransform at = g2d.getTransform();
        at.scale(1/environment.getZoom(), 1/environment.getZoom());
        at.translate(environment.getStartLeft(), environment.getStartTop());

        g2d.setTransform(at);
        animationManager.draw(g2d);

        paintLasers(g2d);

        for(ElectroMagneticWave wave: new HashSet<>(emWaves))
            wave.draw(g2d);

        g2d.setColor(Color.BLACK);
        for(int i = 0; i < objects.size(); i++){
            objects.get(i).draw(g2d);
        }
    }

    private void paintLasers(Graphics2D g2d)
    {
        // TODO replace intensity with proper range/
        for(LaserInformation laser: new ArrayList<>(laserDrawTimeMap.keySet()))
            PaintDrawer.DrawLaser(g2d, laser, laserDrawTimeMap.get(laser) / LASER_DRAW_TIME);

//                g2d.fillOval((int)(laserList.get(i).getPosition().x - laserSize / 2), (int)(laserList.get(i).getPosition().y - laserSize / 2), laserSize, laserSize);
    }

    public static void main(String[] args){
        JFrame frame = new JFrame("AAA");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setBounds(0, 0, 500, 500);

        GamePanel panel = new GamePanel(null);

        frame.add(panel);

        frame.setVisible(true);
    }
}
