package net.minecraft.src;

public class ServerCommand
{
    /** The command string. */
    public final String command;

    /** The CommandListener object associated with this command. */
    public final ICommandListener commandListener;

    public ServerCommand(String par1Str, ICommandListener par2ICommandListener)
    {
        command = par1Str;
        commandListener = par2ICommandListener;
    }
}
