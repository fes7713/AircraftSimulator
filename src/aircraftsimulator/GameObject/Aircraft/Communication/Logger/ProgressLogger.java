package aircraftsimulator.GameObject.Aircraft.Communication.Logger;

import java.util.*;
import java.util.stream.IntStream;

public class ProgressLogger {
    private static final class ProgressData {
        protected final int totalSize;
        protected final long startedTime;
        protected int progress;

        private ProgressData(int totalSize, long startedTime) {
            this.totalSize = totalSize;
            this.startedTime = startedTime;
        }

        public float percentage()
        {
            return progress / (float)totalSize;
        }

        public long timeTaken()
        {
            return (long)((System.currentTimeMillis() - startedTime) / 1000F);
        }

        public long eta()
        {
            return (long)(timeTaken() / percentage() - timeTaken()) ;
        }
    }

    private static final Map<String, ProgressData> progressMap = new HashMap<>();

    public static void AddProgressData(String identifier, int totalSize)
    {
        progressMap.put(identifier, new ProgressData(totalSize, System.currentTimeMillis()));
    }

    public static void PutProgressData(String identifier, int progress)
    {
        progressMap.get(identifier).progress = progress;
    }

    public static void RemoveProgressData(String identifier)
    {
        progressMap.remove(identifier);
    }

    public static void PrintProgress(String identifier)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%6s : %3.1f%% [", identifier.substring(0, Math.min(6, identifier.length())), progressMap.get(identifier).percentage() * 100));
        int num = (int)(30 * progressMap.get(identifier).percentage());
        IntStream.range(0, num).forEach(i -> {sb.append("=");});
        sb.append(">");
        IntStream.range(0, 30 - num).forEach(i -> {sb.append(" ");});
        sb.append(String.format("] %d/%d [%02d:%02d/%02d:%02d]",
                progressMap.get(identifier).progress,
                progressMap.get(identifier).totalSize,
                progressMap.get(identifier).eta() / 60,
                progressMap.get(identifier).eta() % 60,
                progressMap.get(identifier).timeTaken() / 60,
                progressMap.get(identifier).timeTaken() % 60));
        sb.append("\r");
        System.out.print(sb);
    }

    public static void main(String[] args) throws InterruptedException {
        ProgressLogger.AddProgressData("AA", 100);
        for(int i = 1; i <= 100; i++)
        {
            Thread.sleep(100);
            ProgressLogger.PutProgressData("AA", i);
            ProgressLogger.PrintProgress("AA");
        }

    }
}
