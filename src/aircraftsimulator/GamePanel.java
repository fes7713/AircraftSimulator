package aircraftsimulator;

import aircraftsimulator.GameObject.GameObject;

import javax.swing.*;
import javax.vecmath.Vector2f;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;

public class GamePanel extends JPanel {
    private final aircraftsimulator.Environment environment;
    List<GameObject> objects;

    public GamePanel(Environment environment){
        this.environment = environment;
        objects = new ArrayList<>();
        objects.add(new GameObject(new Vector2f(100, 100), Color.BLACK, 5));
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
