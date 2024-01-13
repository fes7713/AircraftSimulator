package aircraftsimulator;

import aircraftsimulator.Animation.AnimationManager;
import aircraftsimulator.GameObject.Aircraft.Aircraft;
import aircraftsimulator.GameObject.Aircraft.Communication.Information.LaserInformation;
import aircraftsimulator.GameObject.Aircraft.FlightController.SwitchValueFlightController;
import aircraftsimulator.GameObject.Aircraft.GuidedMissile;
import aircraftsimulator.GameObject.Aircraft.Missile;
import aircraftsimulator.GameObject.Aircraft.Radar.Radar.AngleRadar;
import aircraftsimulator.GameObject.Aircraft.Radar.RadarFrequency;
import aircraftsimulator.GameObject.Aircraft.Spawner.Gun;
import aircraftsimulator.GameObject.Aircraft.Spawner.MissileLauncher;
import aircraftsimulator.GameObject.Aircraft.Thruster.VariableThruster;
import aircraftsimulator.GameObject.GameObject;
import aircraftsimulator.GameObject.Team;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.vecmath.Vector3f;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class GamePanel extends JPanel {
    private final aircraftsimulator.Environment environment;
    private final List<GameObject> objects;
    private final Map<Float, List<LaserInformation>> laserMap;
    private final Map<Team, List<GameObject>> teamDetectionObjectMap;
    private final AnimationManager animationManager;

    public GamePanel(Environment environment){
        this.environment = environment;
        animationManager = AnimationManager.getInstance();
        objects = new ArrayList<>();
        teamDetectionObjectMap = new HashMap<>();
        laserMap = new HashMap<>();
//        objects.add(new GameObject(new Vector3f(100, 100, 100), Color.CYAN, 5));
        Team A = newTeam();
        Team B = newTeam();
        Team C = newTeam();
        Aircraft aircraftAcc = new Aircraft(A,
                new SwitchValueFlightController<>(),
                new Vector3f(100, 100, 100),
                new Vector3f(1, 0, 0), Color.ORANGE, 5, 100,
                Aircraft.THRUSTER_MAGNITUDE * 2);

        aircraftAcc.setThruster(new VariableThruster(aircraftAcc, Aircraft.THRUSTER_MAGNITUDE * 2, 3600));
//        aircraftAcc.addComponent(new SimpleRadar(aircraftAcc, 100, info -> {
//            aircraftAcc.receive(info);
//        }));

        aircraftAcc.addComponent(new AngleRadar(aircraftAcc, RadarFrequency.X, 1000, 80, aircraftAcc.getDirection()));
        aircraftAcc.addComponent(new Gun(aircraftAcc, 0.2F, 2, 50));

        Missile missile = new GuidedMissile(A, 100, 80);
        aircraftAcc.addComponent(new MissileLauncher(aircraftAcc, missile, 1F, 6));

        Aircraft aircraftAcc1 = new Aircraft(B,
                new SwitchValueFlightController<>(),
                new Vector3f(1000, 300, 100),
                new Vector3f(-1, 0, 0), Color.BLUE, 5, 100,
                Aircraft.THRUSTER_MAGNITUDE * 2);

        aircraftAcc1.setThruster(new VariableThruster(aircraftAcc1, Aircraft.THRUSTER_MAGNITUDE * 2, 3600));
//        aircraftAcc.addComponent(new SimpleRadar(aircraftAcc, 100, info -> {
//            aircraftAcc.receive(info);
//        }));

        aircraftAcc1.addComponent(new AngleRadar(aircraftAcc1, RadarFrequency.X,1000, 60, aircraftAcc1.getDirection()));
        aircraftAcc1.addComponent(new Gun(aircraftAcc1, 0.2F, 2, 50));

        Missile missile1 = new GuidedMissile(B, 10, 80);
        aircraftAcc1.addComponent(new MissileLauncher(aircraftAcc1, missile1, 4, 2));

        Aircraft aircraftAcc2 = aircraftAcc1.clone();
        aircraftAcc2.activate(
                new Vector3f(1000, 90, 100),
                new Vector3f(-1, 0, 0),
                new Vector3f(-1, 0, 0)
        );

//        Missile missile1 = new Missile(A, aircraft1.send(MotionInformation.class), new Vector3f(90, 95, 100),
//                new Vector3f(16, 0, 0), 100, 129);
//        aircraftAcc.addToNetwork(missile1);

//        DestructibleStationaryObject target = new DestructibleStationaryObject(C, new Vector3f(100, 500, 100), Color.GREEN, 5, 100);

        Stream.of(aircraftAcc1, aircraftAcc, aircraftAcc2).forEach(this::addObject);
    }

    public void update(float delta)
    {
        laserMap.clear();
        animationManager.update(delta);
        for(int i = 0; i < objects.size(); i++)
            objects.get(i).componentUpdate(delta);

        for(int i = 0; i < objects.size(); i++)
            objects.get(i).update(delta);

    }

    public Team newTeam()
    {
        Team team = new Team("" + (teamDetectionObjectMap.size() + 1));
        teamDetectionObjectMap.put(team, new ArrayList<>());
        return team;
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
    }

    public List<LaserInformation> getLasers(float frequency)
    {
        return laserMap.get(frequency);
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

        g2d.setColor(Color.BLACK);
        for(int i = 0; i < objects.size(); i++){
            objects.get(i).draw(g2d);
        }
    }

    private void paintLasers(Graphics2D g2d)
    {
        // TODO replace intensity with proper range/
        for(List<LaserInformation> laserList: laserMap.values())
            for(int i = 0; i < laserList.size(); i++)
                PaintDrawer.DrawLaser(g2d, laserList.get(i));

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
