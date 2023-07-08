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
                env.updateGame(20 / 1000F);
                frame.repaint();
                Game.frames++;
                prevTime = System.currentTimeMillis();
                System.out.println(getFrames());
                try {
                    Thread.sleep(3);
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
