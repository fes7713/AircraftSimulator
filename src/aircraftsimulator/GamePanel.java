package aircraftsimulator;

import javax.swing.*;
import java.util.Timer;
import java.util.TimerTask;
import map.NoiseMapPanel;

public class GamePanel extends JPanel {
    public static void main(String[] args)
    {
        JFrame frame = new JFrame("New Frame");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        NoiseMapPanel panel = new NoiseMapPanel() ;
        NoiseMapPanel cloud = new NoiseMapPanel() ;
        NoiseMapPanel cloudHigh = new NoiseMapPanel() ;
        cloud.setResolutionMin(-4);
        cloud.setResolutionMax(1);
        cloud.loadColorPreset("cloud3.txt");
        cloud.loadVariables("cloud2.txt");
        cloud.clearChunks();
        cloud.updateChunkGroups();
        cloud.setOpaque(false);

        cloudHigh.setResolutionMin(-6);
        cloudHigh.setResolutionMax(-4);
        cloudHigh.loadColorPreset("cloud3.txt");
        cloudHigh.loadVariables("cloudHigh1.txt");
        cloudHigh.clearChunks();
        cloudHigh.updateChunkGroups();
        cloudHigh.setOpaque(false);


        OverlayLayout layout = new OverlayLayout(panel);
        panel.setLayout(layout);
        panel.add(cloud);
        panel.add(cloudHigh);
//
        cloud.addComponentListener(panel);
        cloud.addMouseMotionListener(panel);
        cloud.addMouseListener(panel);
        cloud.addMouseWheelListener(panel);

        cloud.addComponentListener(cloudHigh);
        cloud.addMouseMotionListener(cloudHigh);
        cloud.addMouseListener(cloudHigh);
        cloud.addMouseWheelListener(cloudHigh);


//        panel.add(cloud);


        cloud.showColorEditor();
        cloudHigh.showVariableChanger();
//        panel.showColorEditor();
//        panel.showVariableChanger();
//        panel.showLightingChanger();
//        panel.showMapEditor();
//        panel.repaint();

        java.util.Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                cloud.setCenterX(cloud.getCenterX() + 2);
                cloud.setStartLeft(cloud.getStartLeft() - 2);
                cloud.repaint();

                cloudHigh.setCenterX(cloudHigh.getCenterX() + 0.6F);
                cloudHigh.setStartLeft(cloudHigh.getStartLeft() - 0.6F);
                cloudHigh.repaint();
            }
        }, 1000, 50);
        frame.add(panel);
        frame.setBounds(0, 0, 500, 500);
        frame.setVisible(true);
//        frame.repaint();

    }
}
