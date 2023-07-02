package aircraftsimulator.Animation.AnimationGroup;

import aircraftsimulator.Animation.AnimationInterface;
import aircraftsimulator.Animation.TextAnimation.TextAnimation;

import javax.swing.*;
import javax.vecmath.Vector2f;
import java.awt.*;

public class VerticalAnimationGroup extends AnimationGroup {
    public VerticalAnimationGroup(Vector2f position) {
        super(position, null, 0);
    }

    public VerticalAnimationGroup(Vector2f position, Color color, float lifespan) {
        super(position, color, lifespan);
    }

    @Override
    public void draw(Graphics2D g2d) {
        for(int i = 0; i < animations.size(); i++)
            animations.get(i).getPosition().set(position.x, position.y + g2d.getFontMetrics().getHeight() * i);

        super.draw(g2d);
    }

    @Override
    public void addAnimation(AnimationInterface animationInterface) {
        super.addAnimation(animationInterface);
    }

    public static int counter = 0;
    public static void main(String[] args)
    {
        VerticalAnimationGroup vag = new VerticalAnimationGroup(new Vector2f(20, 20));

        JFrame frame = new JFrame("Text Animation Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(new Dimension(1000, 1000));
        JPanel panel = new JPanel()
        {
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D)g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                vag.draw(g2d);
            }
        };
        panel.setBackground(Color.GRAY);
        frame.add(panel);
        frame.setVisible(true);

        new Thread(() -> {
            while(true)
            {
                counter++;
                TextAnimation textAnimation = new TextAnimation("Sample" + counter, new Vector2f(20, 20), Color.GREEN, 2, 0.5F);
                textAnimation.setAnimationEndSignal(animation -> {
                    System.out.println("Ended" + vag.size());
                    vag.removeAnimation(animation);
                });

                vag.addAnimation(textAnimation);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }


        }).start();

        while(true)
        {
            vag.update(10/1000f);
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
