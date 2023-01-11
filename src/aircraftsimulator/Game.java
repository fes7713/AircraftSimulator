package aircraftsimulator;

import javax.swing.*;

public class Game {
    public static void main(String[] args)
    {
        Environment env = new Environment();
        JFrame frame = new JFrame("New Frame");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.add(env);
        frame.setBounds(0, 0, 500, 500);
        frame.setVisible(true);
    }
}
