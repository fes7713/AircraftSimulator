package aircraftsimulator;

import map.NoiseMapPanel;

import javax.swing.*;
import java.util.Timer;
import java.util.TimerTask;

public class Environment {
    private final JFrame frame;

    private final NoiseMapPanel mapPanel;
    private final NoiseMapPanel cloud;
    private final NoiseMapPanel cloudHigh;

    private final Wind windLow;
    private final Wind windHigh;

    public Environment(){
        frame = new JFrame("New Frame");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        mapPanel = new NoiseMapPanel() ;
        cloud = new NoiseMapPanel() ;
        cloudHigh = new NoiseMapPanel() ;
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


        OverlayLayout layout = new OverlayLayout(mapPanel);
        mapPanel.setLayout(layout);
        mapPanel.add(cloud);
        mapPanel.add(cloudHigh);
//
        cloud.addComponentListener(mapPanel);
        cloud.addMouseMotionListener(mapPanel);
        cloud.addMouseListener(mapPanel);
        cloud.addMouseWheelListener(mapPanel);

        cloud.addComponentListener(cloudHigh);
        cloud.addMouseMotionListener(cloudHigh);
        cloud.addMouseListener(cloudHigh);
        cloud.addMouseWheelListener(cloudHigh);

//        cloud.showColorEditor();
//        cloudHigh.showVariableChanger();
//        mapPanel.showColorEditor();
//        mapPanel.showVariableChanger();
//        mapPanel.showLightingChanger();
//        mapPanel.showMapEditor();
//        mapPanel.repaint();

//
//
        frame.add(mapPanel);
        frame.setBounds(0, 0, 500, 500);
        frame.setVisible(true);

        windLow = new Wind(1, 2);
        windHigh = new Wind(0.1F, 0.2F);

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
    }
}
