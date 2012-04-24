package net.minecraft.src;

import net.minecraft.server.MinecraftServer;

public class ThreadServerSleep extends Thread
{
    /** A reference to the Minecraft object. */
    final MinecraftServer mc;

    public ThreadServerSleep(MinecraftServer par1MinecraftServer)
    {
        mc = par1MinecraftServer;
        setDaemon(true);
        start();
    }

    public void run()
    {
        do
        {
            try
            {
                Thread.sleep(0x7fffffffL);
            }
            catch (InterruptedException interruptedexception) { }
        }
        while (true);
    }
}
