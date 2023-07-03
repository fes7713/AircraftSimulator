package aircraftsimulator.Animation.TextAnimation;

import aircraftsimulator.Animation.AnimationGroup.VerticalAnimationGroup;

import javax.swing.*;
import javax.vecmath.Vector2f;
import java.awt.*;

public class TextAnimationGroup extends VerticalAnimationGroup {
    private final float hideStartPercentage;

    public TextAnimationGroup(Vector2f position, Color color, float lifespan, float hideStartPercentage) {
        super(position, color, lifespan);
        this.hideStartPercentage = hideStartPercentage;

    }

    public void createNewText(String text)
    {
        createNewText(text, color, lifespan, hideStartPercentage);
    }

    public void createNewText(String text, Color color, float lifespan, float hideStartPercentage)
    {
        TextAnimation textAnimation = new TextAnimation(text, new Vector2f(0, 0), color,  lifespan,  hideStartPercentage);

        textAnimation.setAnimationEndSignal(animation -> {
            System.out.println("Ended" + size());
            removeAnimation(animation);
        });
        addAnimation(textAnimation);
    }

    public float getRowHeight(Graphics2D g2d)
    {
        return g2d.getFontMetrics().getHeight();
    }

    public static void main(String[] args)
    {
        TextAnimationGroup tag = new TextAnimationGroup(new Vector2f(20, 20), Color.MAGENTA, 2, 0.5F);

        JFrame frame = new JFrame("Text Animation Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(new Dimension(1000, 1000));
        JPanel panel = new JPanel()
        {
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D)g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                tag.draw(g2d);
            }
        };
        panel.setBackground(Color.GRAY);
        frame.add(panel);
        frame.setVisible(true);

        new Thread(() -> {
            while(true)
            {
                counter++;
                tag.createNewText("Sample" + counter);

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }


        }).start();

        while(true)
        {
            tag.update(10/1000f);
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
