package aircraftsimulator;

import aircraftsimulator.GameObject.Aircraft.Communication.Information.LaserInformation;
import aircraftsimulator.GameObject.GameObject;
import aircraftsimulator.GameObject.Team;
import map.NoiseMapPanel;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Timer;

public class Environment extends JPanel{
    private final NoiseMapPanel mapPanel;
    private static Environment environment;

    private final GamePanel gamePanel;

    private Timer timer;

    private Environment(){
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

    public List<GameObject> getObjects(Team team) {
        return gamePanel.getObjects(team);
    }

    public void addObject(GameObject o) {
        gamePanel.addObject(o);
    }

    public void removeObject(GameObject o)
    {
        gamePanel.removeObject(o);
    }

    public void addLaser(LaserInformation o)
    {
        gamePanel.addLaser(o);
    }

    public List<LaserInformation> getLasers(float frequency)
    {
        return gamePanel.getLasers(frequency);
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

    public static Environment getInstance(){
        if(environment == null)
            environment = new Environment();
        return environment;
    }
}
