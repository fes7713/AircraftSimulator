package aircraftsimulator;

import map.NoiseMapPanel;

import javax.swing.*;
import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

public class Environment extends JPanel{
    private final NoiseMapPanel mapPanel;
    private final NoiseMapPanel cloudLow;
    private final NoiseMapPanel cloudHigh;

    private final Wind windLow;
    private final Wind windHigh;

    private final GamePanel gamePanel;

    private final Timer timer;

    public Environment(){
        mapPanel = new NoiseMapPanel() ;
        cloudLow = new NoiseMapPanel() ;
        cloudHigh = new NoiseMapPanel() ;
        gamePanel = new GamePanel(this);
        gamePanel.setOpaque(false);


        cloudLow.setResolutionMin(-4);
        cloudLow.setResolutionMax(1);
        cloudLow.loadColorPreset("cloud3.txt");
        cloudLow.loadVariables("cloud2.txt");
        cloudLow.clearChunks();
        cloudLow.updateChunkGroups();
        cloudLow.setOpaque(false);

        cloudHigh.setResolutionMin(-6);
        cloudHigh.setResolutionMax(-4);
        cloudHigh.loadColorPreset("cloud3.txt");
        cloudHigh.loadVariables("cloudHigh1.txt");
        cloudHigh.clearChunks();
        cloudHigh.updateChunkGroups();
        cloudHigh.setOpaque(false);


        OverlayLayout layout = new OverlayLayout(mapPanel);
        mapPanel.setLayout(layout);
        mapPanel.add(gamePanel);
        mapPanel.add(cloudHigh);
        mapPanel.add(cloudLow);
//
        cloudHigh.addComponentListener(mapPanel);
        cloudHigh.addMouseMotionListener(mapPanel);
        cloudHigh.addMouseListener(mapPanel);
        cloudHigh.addMouseWheelListener(mapPanel);

        cloudHigh.addComponentListener(cloudLow);
        cloudHigh.addMouseMotionListener(cloudLow);
        cloudHigh.addMouseListener(cloudLow);
        cloudHigh.addMouseWheelListener(cloudLow);

//        cloudLow.showColorEditor();
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
                cloudLow.setStartLeft(cloudLow.getStartLeft() - windLow.getWindX());
                cloudLow.setStartTop(cloudLow.getStartTop() - windLow.getWindY());
                cloudLow.setCenter(cloudLow.getCenterX() + windLow.getWindX(), cloudLow.getCenterY() + windLow.getWindY());

                cloudLow.repaint();

                cloudHigh.setStartLeft(cloudHigh.getStartLeft() - windHigh.getWindX());
                cloudHigh.setStartTop(cloudHigh.getStartTop() - windHigh.getWindY());
                cloudHigh.setCenter(cloudHigh.getCenterX() + windHigh.getWindX(), cloudHigh.getCenterY() + windHigh.getWindY());

                cloudHigh.repaint();
            }
        }, 1000, 50);
    }

    public float getGameX(int screenX)
    {
        return mapPanel.getGameX(screenX);
    }

    public float getGameY(int screenY)
    {
        return mapPanel.getGameY(screenY);
    }

    public float getGameSize(int screenSize)
    {
        return mapPanel.getGameSize(screenSize);
    }

    public int getScreenX(float gameX)
    {
        return mapPanel.getScreenX(gameX);
    }

    public int getScreenY(float gameY)
    {
        return mapPanel.getScreenY(gameY);
    }

    public int getScreenSize(float gameSize)
    {
        return mapPanel.getScreenSize(gameSize);
    }

}
