package aircraftsimulator.GameObject.Aircraft.CentralStrategy;

import java.awt.*;

public enum TrackingState {
    UNIDENTIFIED(Color.ORANGE),
    FRIENDLY(Color.GREEN),
    CIVILIAN(Color.GRAY),
    LOST(Color.GRAY),
    ENEMY(Color.RED),
    ENEMY_LOST(Color.PINK);

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
