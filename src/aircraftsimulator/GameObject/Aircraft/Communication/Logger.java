package aircraftsimulator.GameObject.Aircraft.Communication;

public class Logger {
    public static LogLevel Log_Filter = LogLevel.INFO;

    public static void Log(LogLevel log, String message, String myMac, String destMac, Integer port){
        switch (Log_Filter)
        {
            case INFO -> {
                if (log == LogLevel.DEBUG)
                    return;
            }
            case ERROR -> {
                if (log == LogLevel.INFO || log == LogLevel.DEBUG)
                    return;
            }
            case DEBUG -> {
            }
        }
        if(log == LogLevel.ERROR)
            System.err.printf("[%-6s] [%6s-%6s] Port [%d] %s\n", log.name(), myMac.substring(0, Math.min(myMac.length(), 6)), destMac.substring(0, Math.min(destMac.length(), 6)), port, message);
        else
            System.out.printf("[%-6s] [%6s-%6s] Port [%d] %s\n", log.name(), myMac.substring(0, Math.min(myMac.length(), 6)), destMac.substring(0, Math.min(destMac.length(), 6)), port, message);
    }

    public static void Log(LogLevel log, String message, String myMac, Integer port){
        Log(log, message, myMac, "", port);
    }

    public enum LogLevel
    {
        INFO, ERROR, DEBUG
    }
}
