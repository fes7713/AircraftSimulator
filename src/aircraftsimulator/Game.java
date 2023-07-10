package aircraftsimulator;

import javax.swing.*;
import java.util.Timer;

public class Game {
    public static long prevTime;
    public static int frames = 0;
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
                env.updateGame(5 / 1000F);
                frame.repaint();
                Game.frames++;
                prevTime = System.currentTimeMillis();
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static int getFrames() {
        return frames;
    }
}
