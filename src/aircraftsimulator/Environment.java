package aircraftsimulator;

import map.NoiseMapPanel;

import javax.swing.*;
import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

public class Environment extends JPanel{
    private final NoiseMapPanel mapPanel;

    private GamePanel gamePanel;

    private Timer timer;

    public Environment(){
        mapPanel = new NoiseMapPanel() ;
        mapPanel.showCursorGraphics();
        gamePanel = new GamePanel(this);
        gamePanel.setOpaque(false);
        OverlayLayout layout = new OverlayLayout(mapPanel);
        mapPanel.setLayout(layout);
        mapPanel.add(gamePanel);
        this.setLayout(new BorderLayout());
        this.add(mapPanel);
    }

    public void updateGame(float delta)
    {
        gamePanel.update(delta);
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

    public float getZoom(){
        return mapPanel.getZoom();
    }

    public float getStartLeft()
    {
        return mapPanel.getStartLeft();
    }

    public float getStartTop()
    {
        return mapPanel.getStartTop();
    }
}
