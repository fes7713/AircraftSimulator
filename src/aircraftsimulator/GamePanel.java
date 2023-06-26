package aircraftsimulator;

import aircraftsimulator.GameObject.Aircraft.Aircraft;
import aircraftsimulator.GameObject.Aircraft.FlightController.AdvancedFlightController;
import aircraftsimulator.GameObject.Aircraft.FlightController.SimpleFlightController;
import aircraftsimulator.GameObject.Aircraft.FlightController.SwitchValueFlightController;
import aircraftsimulator.GameObject.Aircraft.Radar.AngleRadar;
import aircraftsimulator.GameObject.Aircraft.Radar.SimpleRadar;
import aircraftsimulator.GameObject.Aircraft.Thruster.VariableThruster;
import aircraftsimulator.GameObject.DestructibleObject;
import aircraftsimulator.GameObject.GameObject;

import javax.swing.*;
import javax.vecmath.Vector3f;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class GamePanel extends JPanel {
    private final aircraftsimulator.Environment environment;
    List<GameObject> objects;

    public GamePanel(Environment environment){
        this.environment = environment;
        objects = new ArrayList<>();
//        objects.add(new GameObject(new Vector3f(100, 100, 100), Color.CYAN, 5));

        Aircraft aircraftAcc = new Aircraft(
                new SwitchValueFlightController<>(),
                new Vector3f(100, 100, 100),
                new Vector3f(1, 0, 0), Color.ORANGE, 5, 100,
                Aircraft.THRUSTER_MAGNITUDE * 2);

        aircraftAcc.setThruster(new VariableThruster(aircraftAcc, Aircraft.THRUSTER_MAGNITUDE * 2));
//        aircraftAcc.addComponent(new SimpleRadar(aircraftAcc, 100, info -> {
//            aircraftAcc.receive(info);
//        }));

        aircraftAcc.addComponent(new AngleRadar(aircraftAcc, 100, 30, aircraftAcc::getDirection, info -> {
            aircraftAcc.receive(info);
        }));

        Aircraft aircraft1 = new Aircraft(
                new SimpleFlightController(),
                new Vector3f(500, 100, 100),
                new Vector3f(-1, -0.1F, 0),
                Color.BLUE, 5, 100,
                Aircraft.THRUSTER_MAGNITUDE);
        DestructibleObject target = new DestructibleObject(new Vector3f(100, 500, 100), Color.GREEN, 5, 100);
        Stream.of(target, aircraft1, aircraftAcc).forEach(objects::add);
    }

    public void update(float delta)
    {
        for(GameObject go: objects)
            go.update(delta);
    }

    public List<GameObject> getObjects()
    {
        return objects;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.BLACK);
        AffineTransform at = g2d.getTransform();
        at.scale(1/environment.getZoom(), 1/environment.getZoom());
        at.translate(environment.getStartLeft(), environment.getStartTop());

        g2d.setTransform(at);
        for(GameObject object: objects)
        {
            object.draw(g2d);
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
