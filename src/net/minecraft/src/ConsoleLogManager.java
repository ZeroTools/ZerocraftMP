package net.minecraft.src;

import java.util.logging.*;

public class ConsoleLogManager
{
    /** Reference to the logger. */
    public static Logger logger = Logger.getLogger("Minecraft");

    public ConsoleLogManager()
    {
    }

    /**
     * Initialises the console logger.
     */
    public static void init()
    {
        ConsoleLogFormatter consolelogformatter = new ConsoleLogFormatter();
        logger.setUseParentHandlers(false);
        ConsoleHandler consolehandler = new ConsoleHandler();
        consolehandler.setFormatter(consolelogformatter);
        logger.addHandler(consolehandler);

        try
        {
            FileHandler filehandler = new FileHandler("server.log", true);
            filehandler.setFormatter(consolelogformatter);
            logger.addHandler(filehandler);
        }
        catch (Exception exception)
        {
            logger.log(Level.WARNING, "Failed to log to server.log", exception);
        }
    }
}
