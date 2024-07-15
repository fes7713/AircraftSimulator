package aircraftsimulator;

import aircraftsimulator.GameObject.Aircraft.Communication.Information.LaserInformation;
import aircraftsimulator.GameObject.Aircraft.Radar.Wave.ElectroMagneticWave;
import aircraftsimulator.GameObject.GameObject;
import aircraftsimulator.GameObject.Team;
import map.NoiseMapPanel;

import javax.swing.*;
import javax.vecmath.Vector3f;
import java.awt.*;
import java.util.List;
import java.util.Set;

public class Environment extends JPanel{
    private final NoiseMapPanel mapPanel;
    private static Environment environment;

    private final GamePanel gamePanel;

    private Timer timer;


    public final static float ENVIRONMENTAL_WAVE = 1;

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

    public Set<GameObject> getObjects() {
        return gamePanel.getObjects();
    }

    public void addObject(GameObject o) {
        gamePanel.addObject(o);
    }

    public void removeObject(GameObject o)
    {
        gamePanel.removeObject(o);
    }

    public void addLaser(LaserInformation laser, Team team)
    {
        gamePanel.addLaser(laser);

        List<GameObject> objects = environment.getObjects(team);
        for(GameObject o: objects)
        {
            Vector3f targetVector = new Vector3f(o.getPosition());
            targetVector.sub(laser.getPosition());
            float angleCos = laser.getDirection().dot(targetVector) / laser.getDirection().length() / targetVector.length();
            boolean detected = angleCos > Math.cos(Math.toRadians(laser.getAngle() / 2));
            boolean withinRange = targetVector.lengthSquared() < laser.getIntensity() * laser.getIntensity();
            if(detected && withinRange)
                gamePanel.addLaser(o.reflect(laser.getPosition(), laser.getFrequency(), laser.getIntensity()));
        }
    }

    public List<LaserInformation> getLasers(float frequency)
    {
        return gamePanel.getLasers(frequency);
    }

    public void addPulseWave(ElectroMagneticWave wave)
    {
        gamePanel.addPulseWave(wave);
    }

    public void addWaveToSensor(GameObject object, ElectroMagneticWave wave)
    {
        gamePanel.addWaveToSensor(object, wave);
    }

    public List<ElectroMagneticWave> detectWave(GameObject object) {
        return gamePanel.detectEMWave(object);
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
