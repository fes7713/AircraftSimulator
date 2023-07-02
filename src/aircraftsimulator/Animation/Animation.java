package aircraftsimulator.Animation;

import javax.vecmath.Vector2f;
import java.awt.*;

public abstract class Animation implements AnimationInterface{
    protected final Vector2f position;
    protected Color color;

    protected float time;
    private final float lifespan;

    private AnimationEndSignal animationEndSignal;

    public Animation(Vector2f position, Color color, float lifespan) {
        this.position = position;
        if(color != null)
            this.color = new Color(color.getRGB());
        this.lifespan = lifespan;
        time = lifespan;
    }

    @Override
    public Vector2f getPosition()
    {
        return position;
    }

    @Override
    public void update(float delta) {
        time -= delta;
        if(time < 0 && animationEndSignal != null)
        {
            animationEndSignal.finished(this);
            animationEndSignal = null;
        }
    }

    @Override
    public void draw(Graphics2D g2d) {
        g2d.setColor(color);
    }

    @Override
    public void setAnimationEndSignal(AnimationEndSignal animationEndSignal) {
        this.animationEndSignal = animationEndSignal;
    }
}
