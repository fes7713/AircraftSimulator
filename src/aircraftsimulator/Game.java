package aircraftsimulator;

import javax.swing.*;
import java.util.Timer;
import java.util.TimerTask;

public class Game {
    public static long prevTime;
    public static void main(String[] args)
    {
        Environment env = new Environment();
        JFrame frame = new JFrame("New Frame");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.add(env);
        frame.setBounds(0, 0, 500, 500);
        frame.setVisible(true);

        java.util.Timer timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                env.updateGame(20 / 1000F);
                frame.repaint();
                prevTime = System.currentTimeMillis();
            }
        }, 1000, 20);
    }
}
