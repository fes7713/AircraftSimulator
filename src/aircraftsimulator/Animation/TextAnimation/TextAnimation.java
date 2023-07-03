package aircraftsimulator.Animation.TextAnimation;

import aircraftsimulator.Animation.Animation;

import javax.swing.*;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import java.awt.*;

public class TextAnimation extends Animation {
    private final String text;

    private float alpha;
    private final float alphaTime;

    public final static float LIFESPAN = 2F;
    public final  static float HIDE_START_PERCENTAGE = 0.5F;

    private TextAnimation(String text, Vector2f position, Color color) {
        this(text, position, color, LIFESPAN, HIDE_START_PERCENTAGE);
    }

    public TextAnimation(String text, Vector2f position, Color color, float lifespan, float hideStartPercentage) {
        super(position, color, lifespan);
        this.text = text;
        alphaTime = hideStartPercentage * lifespan;
        alpha = 1;
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        if(time < 0)
            alpha = 0;
        else if (time < alphaTime)
            alpha = time / alphaTime;
        else
            alpha = 1;

        float[] c = new float[4];
        color = new Color(color.getColorSpace(), color.getComponents(c), alpha);
    }

    @Override
    public void draw(Graphics2D g2d) {
        super.draw(g2d);
        g2d.drawString(text, (int)position.x, (int)position.y);
    }

    public static TextAnimation make(String text, Vector2f position, Color color)
    {
        return new TextAnimation(text, position, color);
    }

    public static TextAnimation make(String text, Vector3f position, Color color)
    {
        return new TextAnimation(text, new Vector2f(position.x, position.y), color);
    }

    public static void main(String[] args)
    {
        TextAnimation textAnimation = new TextAnimation("Sample", new Vector2f(20, 20), Color.MAGENTA, 2, 0.5F);
        textAnimation.setAnimationEndSignal(animation -> {
            System.out.println("Ended");
        });
        JFrame frame = new JFrame("Text Animation Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(new Dimension(1000, 1000));
        JPanel panel = new JPanel()
        {
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D)g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                textAnimation.draw(g2d);
            }
        };
        panel.setBackground(Color.GRAY);
        frame.add(panel);
        frame.setVisible(true);

        while(true)
        {
            textAnimation.update(10/1000f);
//            System.out.println("Particles : " + system.size());
            // update
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            frame.repaint();
        }
    }
}
