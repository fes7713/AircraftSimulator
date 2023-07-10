package aircraftsimulator;

import aircraftsimulator.Animation.AnimationManager;
import aircraftsimulator.GameObject.Aircraft.Aircraft;
import aircraftsimulator.GameObject.Aircraft.Communication.Information.MotionInformation;
import aircraftsimulator.GameObject.Aircraft.FlightController.SimpleFlightController;
import aircraftsimulator.GameObject.Aircraft.FlightController.SwitchValueFlightController;
import aircraftsimulator.GameObject.Aircraft.Missile;
import aircraftsimulator.GameObject.Aircraft.Spawner.Gun;
import aircraftsimulator.GameObject.Aircraft.Radar.AngleRadar;
import aircraftsimulator.GameObject.Aircraft.Thruster.VariableThruster;
import aircraftsimulator.GameObject.DestructibleStationaryObject;
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
    List<GameObject> objects;
    Map<Team, List<GameObject>> teamDetectionObjectMap;
    AnimationManager animationManager;

    public GamePanel(Environment environment){
        this.environment = environment;
        animationManager = AnimationManager.getInstance();
        objects = new ArrayList<>();
        teamDetectionObjectMap = new HashMap<>();
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

        aircraftAcc.addComponent(new AngleRadar(aircraftAcc, 1000, 60, aircraftAcc.getDirection()));
        aircraftAcc.addComponent(new Gun(aircraftAcc, 0.2F, 2, 50));



        Aircraft aircraft1 = new Aircraft(B,
                new SimpleFlightController(),
                new Vector3f(500, 120, 100),
                new Vector3f(-1, -0F, 0),
                Color.BLUE, 5, 100,
                Aircraft.THRUSTER_MAGNITUDE);

        Missile missile = new Missile(A, aircraft1.send(MotionInformation.class), new Vector3f(100, 105, 100),
                new Vector3f(8, 0, 0), 100, 129);
        aircraftAcc.addToNetwork(missile);

//        Missile missile1 = new Missile(A, aircraft1.send(MotionInformation.class), new Vector3f(90, 95, 100),
//                new Vector3f(16, 0, 0), 100, 129);
//        aircraftAcc.addToNetwork(missile1);

        DestructibleStationaryObject target = new DestructibleStationaryObject(C, new Vector3f(100, 500, 100), Color.GREEN, 5, 100);

        Aircraft aircraftCopy = new Aircraft(aircraftAcc);
        aircraftCopy.activate(
                new Vector3f(120, 120, 100),
                new Vector3f(1, 0, 0),
                new Vector3f(1, 0, 0));
        Stream.of(target, aircraft1, aircraftAcc, aircraftCopy, missile).forEach(this::addObject);
    }

    public void update(float delta)
    {
        animationManager.update(delta);
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

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        AffineTransform at = g2d.getTransform();
        at.scale(1/environment.getZoom(), 1/environment.getZoom());
        at.translate(environment.getStartLeft(), environment.getStartTop());

        g2d.setTransform(at);
        animationManager.draw(g2d);
        g2d.setColor(Color.BLACK);
        for(int i = 0; i < objects.size(); i++){
            objects.get(i).draw(g2d);
        }
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
