package net.minecraft.src;

import java.io.*;
import net.minecraft.server.MinecraftServer;

public class ThreadCommandReader extends Thread
{
    /** Reference to the MinecraftServer object. */
    final MinecraftServer mcServer;

    public ThreadCommandReader(MinecraftServer par1MinecraftServer)
    {
        mcServer = par1MinecraftServer;
    }

    public void run()
    {
        BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(System.in));
        String s = null;

        try
        {
            while (!mcServer.serverStopped && MinecraftServer.isServerRunning(mcServer) && (s = bufferedreader.readLine()) != null)
            {
                mcServer.addCommand(s, mcServer);
            }
        }
        catch (IOException ioexception)
        {
            ioexception.printStackTrace();
        }
    }
}
