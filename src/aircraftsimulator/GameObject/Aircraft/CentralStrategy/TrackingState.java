package aircraftsimulator.GameObject.Aircraft.CentralStrategy;

import java.awt.*;

public enum TrackingState {
    UNIDENTIFIED(Color.ORANGE),
    FRIENDLY(Color.GREEN),
    CIVILIAN(Color.GRAY),
    ENEMY(Color.RED);

    private Color color;

    TrackingState(Color color)
    {
        this.color = color;
    }

    public Color getColor()
    {
        return color;
    }
}
