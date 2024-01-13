package aircraftsimulator.GameObject.Aircraft.Communication.NetwrokAdaptor;

public class DefaultSession implements Session {
    private static int sessionIdGenerator;

    private final int sessionId;
    private float timeout;

    public final static float DEFAULT_TIMEOUT = 5F;

    public DefaultSession()
    {
        sessionId = generateSessionId();
        timeout = DEFAULT_TIMEOUT;
    }

    @Override
    public int getSessionId() {
        return sessionId;
    }

    @Override
    public void updateTimeout(float delta) {
        timeout -= delta;
    }

    @Override
    public float getRemainingTimeout() {
        return timeout > 0 ? timeout : 0;
    }

    @Override
    public boolean isActive() {
        return timeout > 0;
    }

    private static int generateSessionId()
    {
        return sessionIdGenerator++;
    }
}
