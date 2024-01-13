package aircraftsimulator.GameObject.Aircraft.Communication.Event;

public enum EventPriority {
    HIGH(5),
    MEDIUM(3),
    LOW(1);

    private final int priority;

    EventPriority(final int priority) {
        this.priority = priority;
    }

    public int getInt() {
        return this.priority;
    }

    public boolean hasPriorityOver(EventPriority eventPriority)
    {
        return this.priority > eventPriority.priority;
    }
}
