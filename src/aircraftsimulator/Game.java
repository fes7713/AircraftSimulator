package aircraftsimulator;

import javax.swing.*;
import java.util.Timer;

public class Game {
    public static long prevTime;
    public static int frames = 0;

    private static float frameTime = 10 / 1000F;
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
                env.updateGame(frameTime);
                frame.repaint();
                Game.frames++;
                prevTime = System.currentTimeMillis();
                try {
                    Thread.sleep(2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static int getFrames() {
        return frames;
    }
    public static float getGameTime() {
        return frames * frameTime;
    }
    public static float getGameFrameTime()
    {
        return frameTime;
    }
}
