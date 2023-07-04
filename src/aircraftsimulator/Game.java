package aircraftsimulator;

import javax.swing.*;
import java.util.Timer;

public class Game {
    public static long prevTime;
    public static void main(String[] args)
    {
        Environment env = Environment.getInstance();
        JFrame frame = new JFrame("New Frame");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.add(env);
        frame.setBounds(0, 0, 500, 500);
        frame.setVisible(true);

        java.util.Timer timer = new Timer();

        new Thread(()->{
            while(true)
            {
                env.updateGame(20 / 1000F);
                frame.repaint();
                prevTime = System.currentTimeMillis();
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
