package aircraftsimulator;

import javax.swing.*;

public class Game {
    public static void main(String[] args)
    {
        JFrame frame = new JFrame("Title");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setBounds(200, 200, 500, 500);
        JPanel panel = new GamePanel();
        frame.add(panel);
        frame.setVisible(true);
    }
}
