package aircraftsimulator.GameObject.Aircraft.Radar.Wave;

import aircraftsimulator.Environment;
import aircraftsimulator.GameMath;
import aircraftsimulator.GameObject.Aircraft.CentralStrategy.CommunicationData;
import aircraftsimulator.GameObject.Aircraft.Radar.RadarFrequency;
import aircraftsimulator.GameObject.GameObject;
import aircraftsimulator.GameObject.GameObjectInterface;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class ElectroMagneticWave {
    private GameObjectInterface parent;

    private final Vector3f position;
    private final double power;
    private final double frequency;
    private final double wavelength;
    private final Vector3f direction;
    private final float angle;
    private Color color;

    private boolean reflected;
    private CommunicationData data;

    private double virtualRange;
    private double actualRange;
    private double nextRange;

    public final static float LIGHT_SPEED = 30;

    private final static List<Color> colors = new ArrayList<>(){
        {
            add(new Color(200, 200, 200));
            add(new Color(200, 200, 200));
            add(new Color(10, 10, 10));
            add(new Color(49,1,1));
            add(new Color(118,2,1));
            add(new Color(255,16,0));
            add(new Color(255,110,1));
            add(new Color(255,250,2));
            add(new Color(12,254,2));
            add(new Color(1,140,200));
            add(new Color(11,1,255));
            add(new Color(253,0,251));
            add(new Color(115,1,115));
            add(new Color(31,0,31));
        }
    };

    public ElectroMagneticWave(GameObjectInterface parent, Vector3f position, double power, double frequency, Vector3f direction, float angle) {
        this.parent = parent;
        this.position = new Vector3f(position);
        this.power = power;
        this.frequency = frequency;
        wavelength = LIGHT_SPEED / frequency;
        this.direction = direction;
        this.angle = angle;

        color = ElectroMagneticWave.GenerateFrequencyColor(frequency);
        reflected = false;
    }

    public ElectroMagneticWave(Vector3f position, double power, double frequency, Vector3f direction, float angle) {
        this(null, position, power, frequency, direction, angle);
    }

    public ElectroMagneticWave(ElectroMagneticWave wave, GameObjectInterface parent, double power) {
        this(parent, parent.getPosition(), power, wave.frequency, new Vector3f(wave.position), wave.angle);
        reflected = true;
        direction.sub(parent.getPosition());
    }

    public ElectroMagneticWave(GameObjectInterface parent, Vector3f position, double power, double frequency, Vector3f direction, float angle, CommunicationData data) {
        this(parent, position, power, frequency, direction, angle);
        this.data = data;
        reflected = true;
    }

    public double getFrequency()
    {
        return frequency;
    }

    public double getIntensity()
    {
        return power / (4 * Math.PI * actualRange * actualRange);
    }

    public boolean isReflected() {
        return reflected;
    }

    public void update(float delta)
    {
        actualRange += LIGHT_SPEED * delta;
        nextRange = (int)((actualRange - virtualRange) / wavelength) *  wavelength + virtualRange;

        // Update color
//        float[] rgb = new float[4];
//        color.getRGBComponents(rgb);
//        float alpha = (float)((getIntensity() - Environment.ENVIRONMENTAL_WAVE) / (power * gain / (4 * Math.PI) - Environment.ENVIRONMENTAL_WAVE));
//        System.out.println(alpha);
//        color = new Color(rgb[0], rgb[1], rgb[2], alpha);

        if(nextRange - virtualRange > wavelength)
        {
            Set<GameObject> gameObjects = Environment.getInstance().getObjects();
            gameObjects.remove(parent);

            for(GameObject object: gameObjects)
            {
                detect(object.getPosition(), wave -> {
                    if(reflected)
                        Environment.getInstance().addWaveToSensor(object, this);
                    else
                        Environment.getInstance().addPulseWave(new ElectroMagneticWave(wave, object, getIntensity() * object.getRCS()));
                });
            }
            virtualRange = nextRange;
        }
    }

    public void detect(Vector3f position, Consumer<ElectroMagneticWave> waveConsumer) {
        Vector3f diff = new Vector3f(position);
        diff.sub(this.position);
        float length = diff.length();

        if(virtualRange < length && length < nextRange)
        {
            float angleCos = direction.dot(diff) / direction.length() / length;
            boolean detected = angleCos > Math.cos(Math.toRadians(angle / 2));
            if(detected)
                waveConsumer.accept(this);
        }
    }

    public Vector3f getPosition(){
        return position;
    }

    public Vector3f getDirection() {
        return new Vector3f(direction);
    }

    public double getRange()
    {
        return actualRange;
    }

    public float getAngle() {
        return angle;
    }

    public Color getColor() {
        return color;
    }

    public CommunicationData getData() {
        return data;
    }

    public void draw(Graphics2D g2d)
    {
        g2d.setColor(color);
        double range = actualRange;
        double centerAngle = GameMath.directionToAngle(new Vector2f(direction.x, direction.y)) % 360;
        g2d.drawArc((int)(position.x - range), (int)(position.y - range), (int)(range * 2), (int)(range * 2), (int)(centerAngle - angle / 2), (int)angle);
    }

    public static Color GenerateFrequencyColor(double frequency)
    {
        float colorPos = (float)((Math.log10(frequency) - Math.log10(RadarFrequency.MIN)) / (Math.log10(RadarFrequency.MAX) - Math.log10(RadarFrequency.MIN)));
        int colorIndex = Math.max(Math.min((int)(colorPos * 13), 12), 0);
        return mixColors(colors.get(colorIndex + 1), colors.get(colorIndex), Math.max(Math.min(colorPos * 13F - colorIndex, 1), 0));
    }

    private static Color mixColors(Color color1, Color color2, double percent){
        double inverse_percent = 1.0 - percent;
        int redPart = (int) (color1.getRed()*percent + color2.getRed()*inverse_percent);
        int greenPart = (int) (color1.getGreen()*percent + color2.getGreen()*inverse_percent);
        int bluePart = (int) (color1.getBlue()*percent + color2.getBlue()*inverse_percent);
        return new Color(redPart, greenPart, bluePart);
    }
}
