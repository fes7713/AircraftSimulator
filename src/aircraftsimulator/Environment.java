package aircraftsimulator;

import map.NoiseMapPanel;

import javax.swing.*;
import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

public class Environment extends JPanel{
    private final NoiseMapPanel mapPanel;
    private final NoiseMapPanel cloud;
    private final NoiseMapPanel cloudHigh;

    private final Wind windLow;
    private final Wind windHigh;

    private final Timer timer;

    public Environment(){
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
        this.setLayout(new BorderLayout());
        this.add(mapPanel);

        windLow = new Wind(1, 2);
        windHigh = new Wind(0.1F, 0.2F);

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                cloud.setStartLeft(cloud.getStartLeft() - windLow.getWindX());
                cloud.setStartTop(cloud.getStartTop() - windLow.getWindY());
                cloud.setCenter(cloud.getCenterX() + windLow.getWindX(), cloud.getCenterY() + windLow.getWindY());

                cloud.repaint();

                cloudHigh.setStartLeft(cloudHigh.getStartLeft() - windHigh.getWindX());
                cloudHigh.setStartTop(cloudHigh.getStartTop() - windHigh.getWindY());
                cloudHigh.setCenter(cloudHigh.getCenterX() + windHigh.getWindX(), cloudHigh.getCenterY() + windHigh.getWindY());

                cloudHigh.repaint();
            }
        }, 1000, 50);
    }
}
