package aircraftsimulator.GameObject.Aircraft.Communication.Timeout;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class TimeoutInformation
{
    private final String sessionId;
    private long startTime;
    private long timeout;
    private final long timeoutStartTime;
    private BiConsumer<String, Integer> timeoutHandler;
    private final Consumer<String> retryTimeoutHandler;

    private int retryNum;
    private final long retryMaxNum;

    private final long timeoutRetryInterval;
    private final double timeoutPowerMultiplier;
    private final double timeoutLinearMultiplier;

    private static final int RETRY_MAX_NUM = 3;
    private static final double TIMEOUT_POWER_MULTIPLIER = 1;
    private static final long TIMEOUT_LINER_MULTIPLIER = 0;

    public TimeoutInformation(String sessionId, long timeoutStartTime, BiConsumer<String, Integer> timeoutHandler, Consumer<String> retryTimeoutHandler)
    {
        this(sessionId, timeoutStartTime, 1, 0.0, 0.0, 1, timeoutHandler, retryTimeoutHandler);
    }

    public TimeoutInformation(String sessionId, long timeoutStartTime, long timeoutRetryInterval, BiConsumer<String, Integer> timeoutHandler, Consumer<String> retryTimeoutHandler)
    {
        this(sessionId, timeoutStartTime, timeoutRetryInterval, TIMEOUT_POWER_MULTIPLIER, TIMEOUT_LINER_MULTIPLIER, RETRY_MAX_NUM, timeoutHandler, retryTimeoutHandler);
    }

    public TimeoutInformation(String sessionId, long timeoutStartTime, long timeoutRetryInterval, double timeoutPowerMultiplier, double timeoutLinearMultiplier, int retryMaxNum, BiConsumer<String, Integer> timeoutHandler, Consumer<String> retryTimeoutHandler){
        this.sessionId = sessionId;
        this.timeoutStartTime = timeoutStartTime;
        this.timeoutRetryInterval = timeoutRetryInterval;
        this.timeoutPowerMultiplier = timeoutPowerMultiplier;
        this.timeoutLinearMultiplier = timeoutLinearMultiplier;
        this.timeoutHandler = timeoutHandler;
        this.retryTimeoutHandler = retryTimeoutHandler;
        this.retryMaxNum = retryMaxNum;

        resetTimeout();
    }

    public void checkTimeout()
    {
        if(isTimeout())
            retryAction();
    }

    public void resetTimeout()
    {
        timeout = timeoutStartTime;
        retryNum = 0;
        startTime = System.currentTimeMillis();
    }

    private boolean isTimeout()
    {
        return startTime + timeout < System.currentTimeMillis();
    }

    private void retryAction()
    {
        if(retryMaxNum >= retryNum)
        {
            if(retryMaxNum <= retryNum && retryTimeoutHandler != null)
            {
                retryTimeoutHandler.accept(sessionId);
                return;
            }
            retryNum++;
            timeout = timeoutStartTime + (int)(timeoutRetryInterval * Math.pow(timeoutPowerMultiplier, retryNum) + retryNum * timeoutRetryInterval * timeoutLinearMultiplier);
//            if(retryMaxNum >= retryNum)
//                System.out.println(timeout);
            timeoutHandler.accept(sessionId, retryNum);
        }
        else
            System.out.println("Remaining timeout retry [remove session!!]");
    }

    public void setTimeoutHandler(BiConsumer<String, Integer> timeoutHandler) {
        this.timeoutHandler = timeoutHandler;
    }
}