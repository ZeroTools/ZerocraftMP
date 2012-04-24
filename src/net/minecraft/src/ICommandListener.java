package net.minecraft.src;

public interface ICommandListener
{
    /**
     * Logs the message with a level of INFO.
     */
    public abstract void log(String s);

    /**
     * Gets the players username.
     */
    public abstract String getUsername();
}
