package aircraftsimulator;

import javax.vecmath.Vector2f;

public class Wind {
    private final Vector2f wind;

    public Wind(Vector2f wind)
    {
        this.wind = wind;
    }
    public Wind(float x, float y)
    {
        this.wind = new Vector2f(x, y);
    }

    public Vector2f getWind()
    {
        return wind;
    }

    public float getWindX()
    {
        return wind.x;
    }

    public float getWindY(){
        return wind.y;
    }
}
