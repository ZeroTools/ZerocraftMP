package net.minecraft.src;

public interface IServer
{
    /**
     * Returns the specified property value as an int, or a default if the property doesn't exist
     */
    public abstract int getIntProperty(String s, int i);

    /**
     * Returns the specified property value as a String, or a default if the property doesn't exist
     */
    public abstract String getStringProperty(String s, String s1);

    /**
     * Saves an Object with the given property name
     */
    public abstract void setProperty(String s, Object obj);

    /**
     * Saves all of the server properties to the properties file
     */
    public abstract void saveProperties();

    /**
     * Returns the filename where server properties are stored
     */
    public abstract String getSettingsFilename();

    /**
     * Returns the server hostname
     */
    public abstract String getHostname();

    /**
     * Returns the server port
     */
    public abstract int getPort();

    /**
     * Returns the server message of the day
     */
    public abstract String getMotd();

    /**
     * Returns the server version string
     */
    public abstract String getVersionString();

    /**
     * Returns the number of players on the server
     */
    public abstract int playersOnline();

    /**
     * Returns the maximum number of players allowed on the server
     */
    public abstract int getMaxPlayers();

    /**
     * Returns a list of usernames of all connected players
     */
    public abstract String[] getPlayerNamesAsList();

    /**
     * Returns the name of the currently loaded world
     */
    public abstract String getWorldName();

    public abstract String getPlugin();

    public abstract void func_40010_o();

    /**
     * Handle a command received by an RCon instance
     */
    public abstract String handleRConCommand(String s);

    /**
     * Returns true if debugging is enabled, false otherwise
     */
    public abstract boolean isDebuggingEnabled();

    /**
     * Logs the message with a level of INFO.
     */
    public abstract void log(String s);

    /**
     * logs the warning same as: logger.warning(String);
     */
    public abstract void logWarning(String s);

    /**
     * Log severe error message
     */
    public abstract void logSevere(String s);

    public abstract void logIn(String s);
}
