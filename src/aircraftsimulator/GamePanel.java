package aircraftsimulator;

import javax.swing.*;
import java.awt.*;

public class GamePanel extends JPanel {
    private final aircraftsimulator.Environment environment;

    public GamePanel(Environment environment){
        this.environment = environment;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.BLACK);
        g2d.fillOval(environment.getScreenX(100), environment.getScreenY(100), environment.getScreenSize(100), environment.getScreenSize(100));
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
