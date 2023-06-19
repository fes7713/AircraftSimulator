package aircraftsimulator;

import aircraftsimulator.GameObject.Aircraft.Aircraft;
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
        objects.add(new GameObject(new Vector3f(100, 100, 100), Color.CYAN, 5));
        Aircraft aircraft = new Aircraft(new Vector3f(100, 100, 100), Color.RED, 5, 100);
        DestructibleObject target = new DestructibleObject(new Vector3f(300, 500, 100), Color.GREEN, 5, 100);
        aircraft.setTarget(target);
        Stream.of(target, aircraft).forEach(go -> {
            objects.add(go);
        });
    }

    public void update(float delta)
    {
        for(GameObject go: objects)
            go.update(delta);
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
