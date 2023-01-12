package aircraftsimulator;

import aircraftsimulator.GameObject.GameObject;

import javax.swing.*;
import javax.vecmath.Vector2f;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GamePanel extends JPanel {
    private final aircraftsimulator.Environment environment;
    List<GameObject> objects;

    public GamePanel(Environment environment){
        this.environment = environment;
        objects = new ArrayList<>();
        objects.add(new GameObject(new Vector2f(0, 100), Color.BLACK, 5));
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.BLACK);
        for(GameObject object: objects)
        {
            g2d.setColor(object.getColor());
            g2d.fillOval(
                    environment.getScreenX(object.getPosition().x - object.getSize() / 2),
                    environment.getScreenY(object.getPosition().y - object.getSize() / 2),
                    environment.getScreenSize(object.getSize()),
                    environment.getScreenSize(object.getSize()));
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
